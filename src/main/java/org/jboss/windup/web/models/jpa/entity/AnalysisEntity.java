package org.jboss.windup.web.models.jpa.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "analysis")
public class AnalysisEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    public String id;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    public Date createdAt;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    public Date updatedAt;

    @Singular
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy = "analysis")
    public List<InputEntity> inputs;

    @Singular
    @ElementCollection
    @Column(name="source")
    @CollectionTable(name = "analysis_sources", joinColumns = {@JoinColumn(name = "analysis_id")})
    public List<String> sources;

    @Singular
    @ElementCollection
    @Column(name="target")
    @CollectionTable(name = "analysis_targets", joinColumns = {@JoinColumn(name = "analysis_id")})
    public List<String> targets;

    @Singular
    @ElementCollection
    @Column(name="package")
    @CollectionTable(name = "analysis_included_packages", joinColumns = {@JoinColumn(name = "analysis_id")})
    public List<String> includedPackages;

    @Singular
    @ElementCollection
    @Column(name="package")
    @CollectionTable(name = "analysis_excluded_packages", joinColumns = {@JoinColumn(name = "analysis_id")})
    public List<String> excludedPackages;

    @Singular
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy = "analysis")
    public List<CustomRuleEntity> customRules;

    @Singular
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy = "analysis")
    public List<CustomLabelEntity> customLabels;

    @Singular
    @ElementCollection
    @MapKeyColumn(name="option")
    @Column(name="enabled")
    @CollectionTable(name = "analysis_single_options", joinColumns = {@JoinColumn(name = "analysis_id")})
    public Map<String, Boolean> singleOptions;

    @Version
    public int version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisEntity that = (AnalysisEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
