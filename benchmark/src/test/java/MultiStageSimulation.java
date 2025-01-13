import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class MultiStageSimulation extends Simulation {
    HttpProtocolBuilder httpProtocol = http     // 请求配置
            .baseUrl("http://localhost:8081")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    // CSV 文件路径
    private static final String CSV_FILE_PATH = "users.csv";

    private void writeToCsv(String filePath, String username, String password, String email) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.append(username).append(",")
                    .append(password).append(",")
                    .append(email).append("\n");
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }

    // 检查 CSV 文件是否为空
    private void ensureCsvIsPopulated() {
        File csvFile = new File(CSV_FILE_PATH);
        if (!csvFile.exists() || csvFile.length() == 0) {
            System.out.println("CSV file is empty. Generating test users...");
            generateTestUsers();
        }
    }

    // 方法：生成测试用户并写入 CSV
    private void generateTestUsers() {
        try {
            FileWriter writer = new FileWriter("src\\test\\resources\\" + CSV_FILE_PATH);
            // 添加 CSV 表头
            writer.append("username,password,email\n");
            for (int i = 0; i < 3000; i++) { // 3000个测试用户
                String username = "user_" + UUID.randomUUID();
                String password = "password_" + UUID.randomUUID();
                String email = username + "@example.com";
                writer.append(username).append(",")
                        .append(password).append(",")
                        .append(email).append("\n");
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error generating users: " + e.getMessage());
        }
    }

    // Feeder：CSV 文件读取
    FeederBuilder.FileBased<String> feeder = csv(CSV_FILE_PATH).circular();

    // 注册用户场景
    ScenarioBuilder registerUsers = scenario("注册用户")
            .feed(feeder) // 从 CSV 文件读取账户数据
            .exec(http("User Registration")
                    .post("/api/auth/register")
                    .body(StringBody("{ \"username\": \"#{username}\", \"password\": \"#{password}\", \"email\": \"#{email}\" }"))
                    .check(status().is(200))
            );


    // 登录，创建项目，请求资源的场景
    ScenarioBuilder loginAndRequest = scenario("登录并创建测试")
            .feed(feeder) // 从 CSV 文件读取账户数据
            .exec(http("User Login")
                    .post("/api/auth/login")
                    .body(StringBody("{ \"username\": \"#{username}\", \"password\": \"#{password}\" }"))
                    .check(jsonPath("$.access_token").saveAs("accessToken")) // 提取 access_token
            )
            .exitHereIfFailed() // 如果登录失败，退出场景
            .pause(1)
            .exec(session -> {
                String randomProjectName = "project_" + UUID.randomUUID();
                return session.set("projectName", randomProjectName);
            })
            .exec(http("Create Project")
                    .post("/api/project/create")
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody("{\n" +
                            "    \"name\": \"project_#{projectName}\",\n" +
                            "    \"startDate\": \"2024-12-26\",\n" +
                            "    \"endDate\": \"2025-01-01\",\n" +
                            "    \"description\": \"自动生成的测试项目\",\n" +
                            "    \"priority\": \"LOW\",\n" +
                            "    \"managerId\": \"1\"\n" +
                            "}"))
                    .check(status().is(200)) // 检查是否创建成功
            )
            .exitHereIfFailed()
            .pause(1)
            .exec(http("Get Project List")
                    .get("/api/project/list/all")
                    .header("Authorization", "Bearer #{accessToken}")
                    .queryParam("page", "1")
                    .queryParam("size", "100")
                    .check(status().is(200)) // 检查状态码
            );

    {
        // stage1
//        ensureCsvIsPopulated(); // 生成的用户名和密码
        // 单独运行每个阶段
        setUp(  // 建议先运行注册，然后去运行登录和请求测试
                // stage2
                registerUsers.injectOpen(
                       rampUsersPerSec(100).to(100).during(30)
                ).protocols(httpProtocol)
                // stage3
//                loginAndRequest.injectOpen(
//                        rampUsersPerSec(0).to(100).during(30), // 1 分钟加压阶段
//                        rampUsersPerSec(100).to(0).during(30)  // 1 分钟减压阶段
//                ).protocols(httpProtocol)
        );
    }
}
