package me.weekbelt.runningflex.modules.society;

import lombok.*;
import me.weekbelt.runningflex.modules.account.Account;
import me.weekbelt.runningflex.modules.account.UserAccount;
import me.weekbelt.runningflex.modules.society.form.SocietyDescriptionForm;
import me.weekbelt.runningflex.modules.tag.Tag;
import me.weekbelt.runningflex.modules.zone.Zone;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
@Entity
public class Society {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

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

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    private int memberCount;

    @Enumerated(EnumType.STRING)
    private SocietyType societyType;

    @Builder
    public Society(String path, String title, String shortDescription, String fullDescription,
                   SocietyType societyType) {
        this.path = path;
        this.title = title;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.societyType = societyType;
    }

    public String getEncodedPath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }

    public void addManager(Account account) {
        this.managers.add(account);
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account)
                && !this.managers.contains(account);
    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }

    public void updateDescription(SocietyDescriptionForm societyDescriptionForm) {
        this.shortDescription = societyDescriptionForm.getShortDescription();
        this.fullDescription = societyDescriptionForm.getFullDescription();
    }

    public void updateSocietyImage(String image) {
        this.image = image;
    }

    public void enableSocietyBanner() {
        this.useBanner = true;
    }

    public void disableSocietyBanner() {
        this.useBanner = false;
    }

    public boolean isManagedBy(Account account) {
        return this.managers.contains(account);
    }

    public void publish() {
        if (this.closed || this.published) {
            throw new RuntimeException("소모임을 공개할 수 없는 상태입니다. 소모임을 이미 공개했거나 종료했습니다.");
        } else {
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        }
    }

    public void close() {
        if (!this.published || this.closed) {
            throw new RuntimeException("소모임을 종료할 수 없습니다. 소모임을 공개하지 않았거나 이미 종료한 소모임입니다.");
        } else {
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingUpdatedDateTime == null ||
                this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void startRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 소모임을 공개하거나 한시간 뒤 다시 시도하세요.");
        }
    }

    public void stopRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 소모임을 공개하거나 한 시간뒤 다시 시도하세요");
        }
    }

    public void updatePath(String newPath) {
        this.path = newPath;
    }

    public void updateTitle(String newTitle) {
        this.title = newTitle;
    }

    public boolean isRemovable() {
        return !this.published; // TODO: 모임을 했던 스터디는 삭제할 수 없다.
    }

    public void addMember(Account account) {
        this.getMembers().add(account);
        this.memberCount++;
    }

    public void removeMember(Account account) {
        this.getMembers().remove(account);
        this.memberCount--;
    }
}
