package com.studyhub.service;

import com.studyhub.model.Post;
import com.studyhub.model.Setting;
import com.studyhub.model.User;
import com.studyhub.repository.PostRepository;
import com.studyhub.repository.SettingRepository;
import com.studyhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostListService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SettingRepository settingRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAllWithRelations();
    }

    public List<Post> filterPosts(String title, String category, String author) {
        return postRepository.filterPosts(title, category, author);
    }

    public List<Setting> getAllCategories() {
        return settingRepository.findAllCategoriesFetch();
    }

    public List<User> getAllAuthors() {
        return userRepository.findAll();
    }

    public Setting findCategoryById(String category) {
        Long id=Long.parseLong(String.valueOf(category));
        return settingRepository.findById(id).get();
    }

    public void savePost(Post post) {
        postRepository.save(post);
    }

    public void switchPostStatus(Long id, String status) {
        Post post=postRepository.findById(id).get();
        if("Published".equals(status)) {
            post.setStatus("Hidden");
        } else post.setStatus("Published");
        postRepository.save(post);
    }

    public Post findPostById(Long id) {
        return postRepository.findById(id).get();
    }
}
