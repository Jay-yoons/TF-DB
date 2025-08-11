package fog.review_service.client;

import fog.review_service.dto.UserInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor

public class UserServiceResponseClient {
    @Value("${user-service.url}")
    private String userServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public UserInfoDto getUserInfo(String userId) {
        String url = userServiceUrl + "/users/" + userId;
        return restTemplate.getForObject(url, UserInfoDto.class);
    }
}
