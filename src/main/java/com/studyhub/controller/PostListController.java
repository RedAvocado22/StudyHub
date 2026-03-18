package com.studyhub.controller;

import com.studyhub.dto.NewPostDTO;
import com.studyhub.model.Post;
import com.studyhub.model.Setting;
import com.studyhub.model.User;
import com.studyhub.security.StudyHubUserDetails;
import com.studyhub.service.PostListService;
import com.studyhub.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/post")
public class PostListController {
    PostListService postListService;
    UserProfileService userProfileService;
    @Autowired
    PostListController(PostListService postListService,UserProfileService userProfileService) {
        this.postListService = postListService;
        this.userProfileService = userProfileService;
    }

    @ModelAttribute
    public void exposeRequestUri(HttpServletRequest request, Model model) {
        model.addAttribute("requestURI", request.getRequestURI());
    }
    @GetMapping("/list")
    public String getPostList(@AuthenticationPrincipal StudyHubUserDetails userDetails,
                              @RequestParam(required = false,name = "title") String title,
                              @RequestParam(required = false,name = "category") String category,
                              @RequestParam(required = false,name = "author") String author,
                              @RequestParam(defaultValue = "updatedAt") String sortBy,
                              @RequestParam(defaultValue = "desc") String direction,
                              Model model) {
        User user=userProfileService.getUserById(userDetails.getUser().getId());

        String searchTitle = (title != null && !title.trim().isEmpty()) ? title : null;
        String searchCategory = (category != null && !category.trim().isEmpty()) ? category : null;
        String searchAuthor = (author != null && !author.trim().isEmpty()) ? author : null;

        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("asc") ?
                org.springframework.data.domain.Sort.by(sortBy).ascending() :
                org.springframework.data.domain.Sort.by(sortBy).descending();

        List<Post> posts = postListService.filterPosts(searchTitle, searchCategory, searchAuthor, sort);
        List<Setting> categories = postListService.getAllCategories();
        List<User> authors = postListService.getAllAuthors();

        model.addAttribute("user",user);
        model.addAttribute("posts",posts);
        model.addAttribute("categories", categories);
        model.addAttribute("authors", authors);

        model.addAttribute("title", title);
        model.addAttribute("category", category);
        model.addAttribute("author", author);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "postList";
    }
    @PostMapping("/filter")
    public String filterPosts( @RequestParam(required = false,name = "title") String title,
                               @RequestParam(required = false,name = "category") String category,
                               @RequestParam(required = false,name = "author") String author,
                               @RequestParam(defaultValue = "updatedAt") String sortBy,
                               @RequestParam(defaultValue = "desc") String direction,
                               RedirectAttributes ra) {
        ra.addAttribute("title", title);
        ra.addAttribute("category", category);
        ra.addAttribute("author", author);
        ra.addAttribute("sortBy", sortBy);
        ra.addAttribute("direction", direction);
        return "redirect:/admin/post/list";
    }

    @GetMapping("/new")
    public String createNewPost(Model model,
                                @AuthenticationPrincipal StudyHubUserDetails userDetails) {
        User user=userProfileService.getUserById(userDetails.getUser().getId());
        List<Setting> categories = postListService.getAllCategories();
        model.addAttribute("user",user);
        model.addAttribute("newPostDTO",new NewPostDTO());
        model.addAttribute("categories",categories);
        return "newPost";
    }

    @PostMapping("/new")
    public String saveNewPost(@Valid @ModelAttribute("newPostDTO") NewPostDTO newPostDTO,
                              BindingResult result,
                              @RequestParam("imageFile") MultipartFile file,
                              @AuthenticationPrincipal StudyHubUserDetails userDetails,
                              Model model) throws IOException {
        User user=userProfileService.getUserById(userDetails.getUser().getId());
        if(result.hasErrors()) {
            List<Setting> categories = postListService.getAllCategories();
            model.addAttribute("user",user);
            model.addAttribute("categories",categories);
            return "newPost";
        }
        Post post=new Post();
        post.setTitle(newPostDTO.getTitle());
        post.setFeaturedPost(newPostDTO.isFeatured());
        post.setCategory(postListService.findCategoryById(newPostDTO.getCategory()));
        post.setAuthor(user);
        post.setStatus(newPostDTO.getStatus());
        post.setContent(newPostDTO.getContent());

        // 2. process save File Thumbnail
        if (file != null && !file.isEmpty()) {
            String uploadDir = "uploads";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String imagePath = "/uploads/" + fileName;
            post.setThumbnail(imagePath);
        }
        postListService.savePost(post);
        return "redirect:/admin/post/list";
    }

    @GetMapping("/switch")
    public String switchPostStatus(@RequestParam(name = "id") Long id,
                                   @RequestParam(name = "status") String status) {
        postListService.switchPostStatus(id,status);
        return "redirect:/admin/post/list";
    }

    @GetMapping("/update/{id}")
    public String updatePost(Model model,
                             @AuthenticationPrincipal StudyHubUserDetails userDetails,
                             @PathVariable("id") Long id) {
        User user=userProfileService.getUserById(userDetails.getUser().getId());
        List<Setting> categories = postListService.getAllCategories();
        NewPostDTO newPostDTO=new NewPostDTO();
        Post post=postListService.findPostById(id);
        newPostDTO.setAuthor(post.getAuthor().getFullName());
        newPostDTO.setTitle(post.getTitle());
        newPostDTO.setContent(post.getContent());
        newPostDTO.setStatus(post.getStatus());
        newPostDTO.setCategory(post.getCategory().getName());
        newPostDTO.setFeatured(post.isFeaturedPost());
        if(post.getThumbnail()!=null && !post.getThumbnail().isEmpty()) {
            newPostDTO.setThumbnail(post.getThumbnail());
        }
        model.addAttribute("user",user);
        model.addAttribute("newPostDTO",newPostDTO);
        model.addAttribute("categories",categories);
        model.addAttribute("id",id);
        return "updatePost";
    }

    @PostMapping("/update")
    public String saveUpdatePost(@Valid @ModelAttribute("newPostDTO") NewPostDTO newPostDTO,
                              BindingResult result,
                              @RequestParam("imageFile") MultipartFile file,
                              @AuthenticationPrincipal StudyHubUserDetails userDetails,
                              @RequestParam("id") Long id,
                              Model model) throws IOException {
        User user=userProfileService.getUserById(userDetails.getUser().getId());
        if(result.hasErrors()) {
            List<Setting> categories = postListService.getAllCategories();
            model.addAttribute("user",user);
            model.addAttribute("categories",categories);
            model.addAttribute("id", id);
            return "updatePost";
        }
        Post post=postListService.findPostById(id);
        post.setTitle(newPostDTO.getTitle());
        post.setFeaturedPost(newPostDTO.isFeatured());
        post.setCategory(postListService.findCategoryById(newPostDTO.getCategory()));
        post.setAuthor(user);
        post.setStatus(newPostDTO.getStatus());
        post.setContent(newPostDTO.getContent());

        // 2. process save File Thumbnail
        if (file != null && !file.isEmpty()) {
            String uploadDir = "uploads";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String imagePath = "/uploads/" + fileName;
            post.setThumbnail(imagePath);
        }
        postListService.savePost(post);
        return "redirect:/admin/post/list";
    }

}
