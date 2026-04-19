package fast.campus.netplix.entity.admin;

import fast.campus.netplix.entity.audit.MutableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "admin")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminEntity extends MutableBaseEntity {
    @Id
    @Column(name = "ADMIN_ID")
    private String adminId;

    @Column(name = "ADMIN_EMAIL", unique = true)
    private String adminEmail;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "ADMIN_NAME")
    private String adminName;

    @Column(name = "ROLE")
    private String role;

    public AdminEntity(String adminEmail, String password, String adminName, String role) {
        this.adminId = UUID.randomUUID().toString();
        this.adminEmail = adminEmail;
        this.password = password;
        this.adminName = adminName;
        this.role = role != null ? role : "ADMIN";
    }
}
