package me.weekbelt.runningflex.modules.society;

import lombok.*;
import me.weekbelt.runningflex.modules.account.Account;
import me.weekbelt.runningflex.modules.account.UserAccount;
import me.weekbelt.runningflex.modules.societyManager.SocietyManager;
import me.weekbelt.runningflex.modules.societyMember.SocietyMember;
import me.weekbelt.runningflex.modules.societyTag.SocietyTag;
import me.weekbelt.runningflex.modules.societyZone.SocietyZone;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
@Entity
public class Society {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "society")
    private List<SocietyManager> societyManagers = new ArrayList<>();

    @OneToMany(mappedBy = "society")
    private List<SocietyMember> societyMembers = new ArrayList<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String image;

    @OneToMany(mappedBy = "society")
    private List<SocietyTag> societyTags = new ArrayList<>();

    @OneToMany(mappedBy = "society")
    private List<SocietyZone> societyZones = new ArrayList<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    public String getEncodedPath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }

    public boolean isJoinable(UserAccount userAccount) {
        return this.isPublished() && this.isRecruiting()
                && !isMember(userAccount)
                && !isManager(userAccount);
    }

    public boolean isMember(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (SocietyMember societyMember : this.societyMembers) {
            if (societyMember.getMember().getId().equals(account.getId())){
                return true;
            }
        }
        return false;
    }

    public boolean isManager(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (SocietyManager societyManager : this.societyManagers) {
            if (societyManager.getManager().getId().equals(account.getId())){
                return true;
            }
        }
        return false;
    }


}
