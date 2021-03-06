package me.weekbelt.runningflex.modules.society.repository;

import me.weekbelt.runningflex.modules.society.Society;
import me.weekbelt.runningflex.modules.society.SocietyType;
import me.weekbelt.runningflex.modules.tag.Tag;
import me.weekbelt.runningflex.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface SocietyRepositoryCustom {

    Page<Society> findByKeyword(String keyword, Pageable pageable);

    List<Society> findByAccount(Set<Tag> tags, Set<Zone> zones);

    Page<Society> findBySocietyType(SocietyType societyType, Pageable pageable);
}
