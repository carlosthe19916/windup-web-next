/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.windup.web.resources;

import org.jboss.windup.web.dto.ContribuyenteDto;
import org.jboss.windup.web.dto.SearchResultDto;
import org.jboss.windup.web.mapper.ContribuyenteMapper;
import org.jboss.windup.web.mapper.SearchResultMapper;
import org.jboss.windup.web.models.FilterBean;
import org.jboss.windup.web.models.PageBean;
import org.jboss.windup.web.models.SearchResultBean;
import org.jboss.windup.web.models.SortBean;
import org.jboss.windup.web.models.TipoPersona;
import org.jboss.windup.web.models.jpa.ContribuyenteRepository;
import org.jboss.windup.web.models.jpa.VersionRepository;
import org.jboss.windup.web.models.jpa.entity.ContribuyenteEntity;
import org.jboss.windup.web.models.jpa.entity.VersionEntity;
import org.jboss.windup.web.resources.interceptors.AllowAdvancedSearch;
import org.jboss.windup.web.security.Permission;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.Max;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Optional;

@Transactional
@ApplicationScoped
@Path("/contribuyentes")
public class ContribuyenteResource {

    @Inject
    VersionRepository versionRepository;

    @Inject
    ContribuyenteRepository contribuyenteRepository;

    @Inject
    SearchResultMapper searchResultMapper;

    @Inject
    ContribuyenteMapper contribuyenteMapper;

    @AllowAdvancedSearch
    @RolesAllowed({Permission.admin, Permission.search})
    @Operation(summary = "Search contribuyentes", description = "Get contribuyentes in a page")
    @GET
    @Path("/")
    @Produces("application/json")
    @Counted(name = "searchContribuyenteChecks", description = "How many times the advanced search was used")
    @Timed(name = "searchContribuyenteTimer", description = "How long it took to serve the advanced search")
    public SearchResultDto<ContribuyenteDto> getContribuyentes(
            @QueryParam("filterText") String filterText,
            @QueryParam("tipoPersona") String tipoPersona,
            @QueryParam("offset") @DefaultValue("0") @Max(9_000) Integer offset,
            @QueryParam("limit") @DefaultValue("10") @Max(1_000) Integer limit,
            @QueryParam("sort_by") List<String> sortBy
    ) {
        Optional<VersionEntity> versionOptional = versionRepository.findActive();
        if (versionOptional.isEmpty()) {
            return SearchResultDto.getEmptyResult(offset, limit);
        }

        VersionEntity version = versionOptional.get();

        PageBean pageBean = PageBean.buildWith(offset, limit);
        List<SortBean> sortBeans = SortBean.buildWith(sortBy, ContribuyenteRepository.SORT_BY_FIELDS);
        FilterBean filterBean = FilterBean.builder()
                .filterText(filterText)
                .tipoPersona(tipoPersona != null ? TipoPersona.valueOf(tipoPersona.toUpperCase()) : null)
                .build();

        SearchResultBean<ContribuyenteEntity> list = contribuyenteRepository.list(version, filterBean, pageBean, sortBeans);

        return searchResultMapper.toDto(list, entity -> contribuyenteMapper.toDto(entity));
    }

    @RolesAllowed({Permission.admin, Permission.search})
    @Operation(summary = "Get contribuyente by numeroDocumento", description = "Get contribuyentes by numeroDocumento")
    @GET
    @Path("/{numeroDocumento}")
    @Produces("application/json")
    @Counted(name = "getContribuyenteChecks", description = "How many times the endpoint was used")
    @Timed(name = "getContribuyenteTimer", description = "How long it took to serve the data")
    public RestResponse<ContribuyenteDto> getContribuyente(@PathParam("numeroDocumento") String numeroDocumento) {
        RestResponse<ContribuyenteDto> notFound = ResponseBuilder.<ContribuyenteDto>notFound().build();

        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return notFound;
        }

        VersionEntity version = versionRepository.findActive().orElseThrow(NotFoundException::new);
        if (numeroDocumento.trim().length() == 11) {
            return contribuyenteRepository.findByRuc(version, numeroDocumento)
                    .map(entity -> contribuyenteMapper.toDto(entity))
                    .map(dto -> ResponseBuilder.ok(dto).build())
                    .orElse(notFound);
        }
        if (numeroDocumento.trim().length() == 8) {
            return contribuyenteRepository.findByDni(version, numeroDocumento)
                    .map(entity -> contribuyenteMapper.toDto(entity))
                    .map(dto -> ResponseBuilder.ok(dto).build())
                    .orElse(notFound);
        } else {
            return notFound;
        }
    }
}
