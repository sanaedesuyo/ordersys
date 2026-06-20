package com.ordersys.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ordersys.user.dto.AddressRequest;
import com.ordersys.user.entity.UserAddress;
import com.ordersys.user.mapper.UserAddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserAddressService extends ServiceImpl<UserAddressMapper, UserAddress> {

    public List<UserAddress> listByUser(Long userId) {
        return lambdaQuery()
            .eq(UserAddress::getUserId, userId)
            .orderByDesc(UserAddress::getIsDefault)
            .orderByDesc(UserAddress::getCreateTime)
            .list();
    }

    public UserAddress getOwned(Long userId, Long addressId) {
        UserAddress address = getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new IllegalArgumentException("地址不存在");
        }
        return address;
    }

    @Transactional
    public UserAddress create(Long userId, AddressRequest req) {
        validateDetail(req.getDetail());

        boolean makeDefault = Boolean.TRUE.equals(req.getIsDefault()) || countByUser(userId) == 0;

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setLabel(normalizeLabel(req.getLabel()));
        address.setDetail(req.getDetail().trim());
        address.setIsDefault(makeDefault);
        save(address);

        if (makeDefault) {
            clearOtherDefaults(userId, address.getId());
        }
        return address;
    }

    @Transactional
    public UserAddress update(Long userId, Long addressId, AddressRequest req) {
        UserAddress address = getOwned(userId, addressId);
        if (StringUtils.hasText(req.getDetail())) {
            address.setDetail(req.getDetail().trim());
        } else {
            validateDetail(address.getDetail());
        }
        if (req.getLabel() != null) {
            address.setLabel(normalizeLabel(req.getLabel()));
        }
        updateById(address);

        if (Boolean.TRUE.equals(req.getIsDefault())) {
            setDefault(userId, addressId);
            address.setIsDefault(true);
        }
        return address;
    }

    @Transactional
    public void delete(Long userId, Long addressId) {
        UserAddress address = getOwned(userId, addressId);
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        removeById(addressId);

        if (wasDefault) {
            UserAddress next = lambdaQuery()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getCreateTime)
                .last("LIMIT 1")
                .one();
            if (next != null) {
                next.setIsDefault(true);
                updateById(next);
            }
        }
    }

    @Transactional
    public UserAddress setDefault(Long userId, Long addressId) {
        getOwned(userId, addressId);
        clearOtherDefaults(userId, addressId);
        UserAddress address = getById(addressId);
        address.setIsDefault(true);
        updateById(address);
        return address;
    }

    public String resolveDeliveryAddress(Long userId, Long addressId) {
        UserAddress address = getOwned(userId, addressId);
        return formatAddress(address);
    }

    public String formatAddress(UserAddress address) {
        if (address == null) return null;
        if (StringUtils.hasText(address.getLabel())) {
            return address.getLabel() + " · " + address.getDetail();
        }
        return address.getDetail();
    }

    private long countByUser(Long userId) {
        return count(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId));
    }

    private void clearOtherDefaults(Long userId, Long keepId) {
        update(new LambdaUpdateWrapper<UserAddress>()
            .eq(UserAddress::getUserId, userId)
            .ne(UserAddress::getId, keepId)
            .set(UserAddress::getIsDefault, false));
    }

    private void validateDetail(String detail) {
        if (!StringUtils.hasText(detail)) {
            throw new IllegalArgumentException("请填写详细地址");
        }
    }

    private String normalizeLabel(String label) {
        return StringUtils.hasText(label) ? label.trim() : null;
    }
}
