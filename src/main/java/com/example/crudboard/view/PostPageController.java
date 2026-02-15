package com.example.crudboard.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/posts")
public class PostPageController {

    @GetMapping
    public String listPage() {
        return "posts/list";
    }

    @GetMapping("/new")
    public String newPage() {
        return "posts/new";
    }

    @GetMapping("/{id}")
    public String detailPage() {
        return "posts/detail";
    }

    @GetMapping("/{id}/edit")
    public String editPage() {
        return "posts/edit";
    }
}
