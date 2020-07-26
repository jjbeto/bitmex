package com.jjbeto.bitmex.api;

import com.jjbeto.bitmex.client.api.UserApi;
import com.jjbeto.bitmex.client.model.Wallet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserApi userApi;

    public UserController(UserApi userApi) {
        this.userApi = userApi;
    }

    @GetMapping("wallet")
    public Wallet getWallet(@RequestParam("symbol") Optional<String> symbol) {
        return userApi.userGetWallet(symbol.orElse("XBt"));
    }

}
