package me.weekbelt.runningflex.modules.tag;

import lombok.*;
import me.weekbelt.runningflex.modules.accountTag.AccountTag;
import me.weekbelt.runningflex.modules.societyTag.SocietyTag;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Builder @AllArgsConstructor @NoArgsConstructor
@Getter @EqualsAndHashCode(of = "id")
@Entity
public class Tag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String title;

    @OneToMany(mappedBy = "tag")
    private List<AccountTag> accountTags = new ArrayList<>();

    @OneToMany(mappedBy = "tag")
    private List<SocietyTag> societyTags = new ArrayList<>();
}