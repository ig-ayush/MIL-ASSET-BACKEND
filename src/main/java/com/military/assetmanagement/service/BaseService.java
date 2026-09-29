package com.military.assetmanagement.service;

import com.military.assetmanagement.audit.AuditService;
import com.military.assetmanagement.dto.base.BaseRequest;
import com.military.assetmanagement.dto.base.BaseResponse;
import com.military.assetmanagement.entity.Base;
import com.military.assetmanagement.entity.User;
import com.military.assetmanagement.exception.BadRequestException;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.BaseRepository;
import com.military.assetmanagement.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaseService {

    private final BaseRepository baseRepository;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    public List<BaseResponse> getAllBases() {
        return baseRepository.findByActiveTrue()
                .stream()
                .map(BaseResponse::from)
                .toList();
    }

    public BaseResponse getBaseById(Long id) {
        return baseRepository.findById(id)
                .map(BaseResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Base", id));
    }

    @Transactional
    public BaseResponse createBase(BaseRequest request) {
        if (baseRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new BadRequestException("Base with name '" + request.name() + "' already exists");
        }

        Base base = Base.builder()
                .name(request.name().trim())
                .location(request.location())
                .description(request.description())
                .active(true)
                .build();

        Base savedBase = baseRepository.save(base);

        User currentUser = getCurrentUserSafely();
        auditService.log(
                "CREATE",
                "Base",
                savedBase.getId(),
                savedBase,
                currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getEmail() : null,
                "Created base: " + savedBase.getName()
        );

        return BaseResponse.from(savedBase);
    }

    @Transactional
    public BaseResponse updateBase(Long id, BaseRequest request) {
        Base base = baseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Base", id));

        String newName = request.name().trim();
        if (!base.getName().equalsIgnoreCase(newName) && baseRepository.existsByNameIgnoreCase(newName)) {
            throw new BadRequestException("Base with name '" + newName + "' already exists");
        }

        base.setName(newName);
        base.setLocation(request.location());
        base.setDescription(request.description());

        Base updatedBase = baseRepository.save(base);

        User currentUser = getCurrentUserSafely();
        auditService.log(
                "UPDATE",
                "Base",
                updatedBase.getId(),
                updatedBase,
                currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getEmail() : null,
                "Updated base: " + updatedBase.getName()
        );

        return BaseResponse.from(updatedBase);
    }

    @Transactional
    public void deleteBase(Long id) {
        Base base = baseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Base", id));

        base.setActive(false);
        baseRepository.save(base);

        User currentUser = getCurrentUserSafely();
        auditService.log(
                "DELETE",
                "Base",
                base.getId(),
                base,
                currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getEmail() : null,
                "Deactivated base: " + base.getName()
        );
    }

    private User getCurrentUserSafely() {
        try {
            return securityUtils.getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }
}
