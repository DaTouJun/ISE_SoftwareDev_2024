package org.flitter.backend.controller;
import org.flitter.backend.entity.DocumentVersion;
import org.flitter.backend.service.CommentService;
import org.flitter.backend.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.flitter.backend.dto.CommentCreateDTO;
import org.flitter.backend.entity.CommentAdapter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private DocumentService documentService;

    //获得task所有评论和文件名
    @GetMapping("/get")
    public ResponseEntity<?> getComment(@RequestParam Long id) {
        try {
            List<CommentAdapter> comments=commentService.getAllComments(id);
            //在这里加一个判断是字符串评论还是文件评论
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.toString());
        }
    }


    //添加字符串评论
    @PostMapping("/add/string")
    public ResponseEntity<?> addStringComment(@RequestBody CommentCreateDTO commentCreateDTO) {
        try {
            CommentAdapter comment = commentService.addStringComment(commentCreateDTO);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.toString());
        }
    }

    //添加文件
    @PostMapping("/add/file")
    public ResponseEntity<?> uploadNewVersion(@RequestParam Long documentId,
                                              @RequestParam Long taskId,
                                              @RequestParam String version,
                                              @RequestParam("file") MultipartFile file) {
        try {
            CommentAdapter comment = commentService.uploadNewVersion(documentId, taskId, version, file);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.toString());
        }
    }
}


// 通过task id 获得所有的评论或者文件名
// 添加评论
// 添加文件（documentcontroll 和service里面找
// TODO：删除评论，删除文件
