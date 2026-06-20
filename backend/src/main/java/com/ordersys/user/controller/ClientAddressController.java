package com.ordersys.user.controller;

import com.ordersys.common.Result;
import com.ordersys.user.dto.AddressRequest;
import com.ordersys.user.entity.UserAddress;
import com.ordersys.user.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/address")
@RequiredArgsConstructor
public class ClientAddressController {

    private final UserAddressService addressService;

    @GetMapping
    public Result<List<UserAddress>> list(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(addressService.listByUser(userId));
    }

    @PostMapping
    public Result<UserAddress> create(@RequestBody AddressRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        try {
            return Result.success(addressService.create(userId, req));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<UserAddress> update(@PathVariable Long id,
                                      @RequestBody AddressRequest req,
                                      Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        try {
            return Result.success(addressService.update(userId, id, req));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        try {
            addressService.delete(userId, id);
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/default")
    public Result<UserAddress> setDefault(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        try {
            return Result.success(addressService.setDefault(userId, id));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
