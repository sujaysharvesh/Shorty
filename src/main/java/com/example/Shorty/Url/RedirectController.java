package com.example.Shorty.Url;


import com.example.Shorty.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode,
                         HttpServletResponse response) throws IOException {

        try {

            String originalUrl = urlService.getOriginalUrl(shortCode);
            response.sendRedirect(originalUrl);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Short Url Not Found");
        }

    }


}
