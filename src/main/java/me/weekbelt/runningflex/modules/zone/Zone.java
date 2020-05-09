package me.weekbelt.runningflex.modules.zone;

import lombok.*;
import me.weekbelt.runningflex.modules.accountZone.AccountZone;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Builder @AllArgsConstructor @NoArgsConstructor
@Getter @EqualsAndHashCode(of = "id")
@Entity
public class Zone {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String localNameOfCity;

    @Column
    private String province;

//    @OneToMany(mappedBy = "zone")
//    private List<AccountZone> accountZones = new ArrayList<>();

//    @OneToMany(mappedBy = "zone")
//    private List<SocietyZone> societyZones = new ArrayList<>();

    @Override
    public String toString() {
        return String.format("%s(%s)/%s", city, localNameOfCity, province);
    }
}
