package com.pal.dipesh.razorpay.merchant.mapper;

import com.pal.dipesh.razorpay.merchant.dto.response.ApiKeyCreateResponse;
import com.pal.dipesh.razorpay.merchant.dto.response.ApiKeyResponse;
import com.pal.dipesh.razorpay.merchant.entity.ApiKey;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiKeyMapper {
    ApiKeyMapper INSTANCE = Mappers.getMapper(ApiKeyMapper.class);

    /**
     * Maps {@link ApiKey} to {@link ApiKeyCreateResponse}.
     *
     * <p>{@code ignoreByDefault = true} flips MapStruct into an "allow-list"
     * mode: nothing is auto-mapped from {@link ApiKey}. Only the targets
     * explicitly listed below are populated. This is a defensive guarantee
     * that no sensitive pojo field (e.g. {@code keySecretHash},
     * {@code webHookSecretHash}) can leak into the DTO, even if names happen
     * to align in the future.
     *
     * <p>The raw secret is supplied by the caller (service layer) as a
     * separate parameter rather than read from the pojo, because the pojo
     * only stores the <em>hash</em>. The raw secret is plaintext and exists
     * only for the lifetime of this single create call.
     *
     * @param apiKey    persisted pojo (must not be {@code null})
     * @param rawSecret freshly generated plaintext secret to expose to the
     *                  caller exactly once; must not be {@code null}
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "apiKey.id")
    @Mapping(target = "keyId", source = "apiKey.keyId")
    @Mapping(target = "environment", source = "apiKey.environment")
    @Mapping(target = "keySecret", source = "rawSecret")
    ApiKeyCreateResponse toApiKeyCreateResponse(ApiKey apiKey, String rawSecret);

    /**
     * Maps {@link ApiKey} to {@link ApiKeyResponse} for read/list endpoints.
     *
     * <p>Like {@link #toApiKeyCreateResponse(ApiKey, String)}, this method
     * uses {@code ignoreByDefault = true} so MapStruct operates in
     * "allow-list" mode: no field is auto-copied from the pojo. Only the
     * non-sensitive, listing-safe attributes explicitly enumerated below are
     * populated.
     *
     * <p>The following sensitive pojo fields are <strong>deliberately
     * excluded</strong> and must never appear on this DTO:
     * <ul>
     *   <li>{@code keySecretHash}, {@code previousKeySecretHash},
     *       {@code webHookSecretHash} &mdash; credential material.</li>
     *   <li>{@code rotatedAt}, {@code rotatedBy}, {@code revokedAt},
     *       {@code revokedBy}, {@code gracePeriodExpiresAt} &mdash; internal
     *       lifecycle / audit metadata not part of the public listing
     *       contract.</li>
     *   <li>{@code merchant} &mdash; tenancy is enforced at the query level;
     *       the merchant association is not echoed back to callers.</li>
     * </ul>
     *
     * <p>Note: the raw (plaintext) secret is never available on a read path
     * &mdash; it exists only at creation/rotation time &mdash; which is why
     * this method has no {@code rawSecret} parameter and the DTO has no
     * {@code keySecret} field.
     *
     * <p>This element-level method is also the one MapStruct invokes per item
     * when implementing the list overload
     * {@link #toApiKeyResponse(List)}; keep it as the single source of truth
     * for field-level mapping rules.
     *
     * @param apiKey persisted pojo (must not be {@code null})
     * @return DTO containing only listing-safe attributes of the API key
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "apiKey.id")
    @Mapping(target = "keyId", source = "apiKey.keyId")
    @Mapping(target = "environment", source = "apiKey.environment")
    @Mapping(target = "enabled", source = "apiKey.enabled")
    @Mapping(target = "lastUsedAt", source = "apiKey.lastUsedAt")
    @Mapping(target = "createdAt", source = "apiKey.createdAt")
    ApiKeyResponse toApiKeyResponse(ApiKey apiKey);

    /**
     * Maps a list of {@link ApiKey} entities to a list of
     * {@link ApiKeyResponse} DTOs.
     *
     * <p>MapStruct generates the implementation as a simple loop that
     * delegates to {@link #toApiKeyResponse(ApiKey)} for each element, so the
     * field-level allow-list and sensitive-field exclusions defined on the
     * element method apply automatically to every item &mdash; there is no
     * separate place where mappings could drift.
     *
     * <p>Null/empty input handling follows MapStruct defaults:
     * <ul>
     *   <li>A {@code null} input list yields {@code null}.</li>
     *   <li>An empty input list yields an empty list.</li>
     *   <li>{@code null} elements inside the list are mapped to {@code null}
     *       entries in the output (the element method is invoked with
     *       {@code null}).</li>
     * </ul>
     *
     * @param apiKeys entities to map; may be {@code null}
     * @return list of listing-safe DTOs in the same order as the input, or
     *         {@code null} if {@code apiKeys} is {@code null}
     */
    List<ApiKeyResponse> toApiKeyResponse(List<ApiKey> apiKeys);
}