package me.weekbelt.runningflex.modules.society.service;

import lombok.RequiredArgsConstructor;
import me.weekbelt.runningflex.modules.account.Account;
import me.weekbelt.runningflex.modules.society.Society;
import me.weekbelt.runningflex.modules.society.event.SocietyCreatedEvent;
import me.weekbelt.runningflex.modules.society.event.SocietyUpdateEvent;
import me.weekbelt.runningflex.modules.society.form.SocietyDescriptionForm;
import me.weekbelt.runningflex.modules.society.repository.SocietyRepository;
import me.weekbelt.runningflex.modules.tag.Tag;
import me.weekbelt.runningflex.modules.zone.Zone;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;

import static me.weekbelt.runningflex.modules.society.form.SocietyForm.VALID_PATH_PATTERN;

@RequiredArgsConstructor
@Transactional
@Service
public class SocietyService {

    private final SocietyRepository societyRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Society createNewSociety(Society society, Account account) {
        Society newSociety = societyRepository.save(society);
        newSociety.addManager(account);
        return newSociety;
    }

    public Society getSocietyByPath(String path) {
        return societyRepository.findByPath(path)
                .orElseThrow(() -> new IllegalArgumentException(path + "에 해당하는 소모임이 없습니다."));
    }

    public Society getSocietyToUpdate(Account account, String path) throws AccessDeniedException {
        Society society = this.getSocietyByPath(path);
        if (!account.isManagerOf(society)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
        return society;
    }

    public void updateSocietyDescription(Society society, SocietyDescriptionForm societyDescriptionForm) {
        society.updateDescription(societyDescriptionForm);
        eventPublisher.publishEvent(new SocietyUpdateEvent(society, "소모임 소개를 수정했습니다."));
    }

    public void updateSocietyImage(Society society, String image) {
        society.updateSocietyImage(image);
    }

    public void enableSocietyBanner(Society society) {
        society.enableSocietyBanner();
    }

    public void disableSocietyBanner(Society society) {
        society.disableSocietyBanner();
    }

    public Society getSocietyToUpdateTag(Account account, String path) throws AccessDeniedException {
        Society society = societyRepository.findAccountWithTagsByPath(path)
                .orElse(null);
        checkIfExistingSociety(path, society);
        checkIfManager(account, society);
        return society;
    }

    public void addTag(Society society, Tag tag) {
        society.getTags().add(tag);
    }

    public void removeTag(Society society, Tag tag) {
        society.getTags().remove(tag);
    }

    public Society getSocietyToUpdateZone(Account account, String path) throws AccessDeniedException {
        Society society = societyRepository.findSocietyWithZonesByPath(path)
                .orElse(null);
        checkIfExistingSociety(path, society);
        checkIfManager(account, society);
        return society;
    }


    public void addZone(Society society, Zone zone) {
        society.getZones().add(zone);
    }

    public void removeZone(Society society, Zone zone) {
        society.getZones().remove(zone);
    }

    public Society getSocietyToUpdateStatus(Account account, String path) throws AccessDeniedException {
        Society society = societyRepository.findSocietyWithManagersByPath(path)
                .orElse(null);
        checkIfExistingSociety(path, society);
        checkIfManager(account, society);
        return society;
    }

    private void checkIfExistingSociety(String path, Society society) {
        if (society == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    private void checkIfManager(Account account, Society society) throws AccessDeniedException {
        if (!society.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    public void publish(Society society) {
        society.publish();
        this.eventPublisher.publishEvent(new SocietyCreatedEvent(society));
    }

    public void close(Society society) {
        society.close();
        eventPublisher.publishEvent(new SocietyUpdateEvent(society, "소모임을 종료했습니다."));
    }

    public void startRecruit(Society society) {
        society.startRecruit();
        eventPublisher.publishEvent(new SocietyUpdateEvent(society, "회원 모집을 시작합니다."));
    }

    public void stopRecruit(Society society) {
        society.stopRecruit();
        eventPublisher.publishEvent(new SocietyUpdateEvent(society, "회원 모집을 중단했습니다."));
    }

    public boolean isValidPath(String newPath) {
        if (!newPath.matches(VALID_PATH_PATTERN)) {
            return false;
        }

        return !societyRepository.existsByPath(newPath);

    }

    public void updateSocietyPath(Society society, String newPath) {
        society.updatePath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void updateSocietyTitle(Society society, String newTitle) {
        society.updateTitle(newTitle);
    }

    public void remove(Society society) {
        if (society.isRemovable()) {
            societyRepository.delete(society);
        } else {
            throw new IllegalArgumentException("소모임을 삭제할 수 없습니다.");
        }
    }

    public void addMembers(Society society, Account account) {
        society.addMember(account);
    }


    public void removeMember(Society society, Account account) {
        society.removeMember(account);
    }

    public Society getSocietyToEnroll(String path) {
        return societyRepository.findSocietyOnlyByPath(path)
                .orElseThrow(() -> new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다."));
    }

}
