package com.military.assetmanagement;

import com.military.assetmanagement.entity.*;
import com.military.assetmanagement.repository.*;
import com.military.assetmanagement.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests that exercise the full application context with H2.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AssetManagementIntegrationTest {

    @Autowired
    private BaseRepository baseRepository;

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssetInventoryRepository assetInventoryRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private ExpenditureRepository expenditureRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private Base base1;
    private Base base2;
    private EquipmentType rifle;
    private User adminUser;

    @BeforeEach
    void setUp() {
        base1 = baseRepository.save(Base.builder()
                .name("Test Base Alpha")
                .location("Test Location A")
                .description("Test base one")
                .active(true)
                .build());

        base2 = baseRepository.save(Base.builder()
                .name("Test Base Beta")
                .location("Test Location B")
                .description("Test base two")
                .active(true)
                .build());

        rifle = equipmentTypeRepository.save(EquipmentType.builder()
                .name("Test Rifle")
                .description("Standard test rifle")
                .unit("unit")
                .active(true)
                .build());

        adminUser = userRepository.save(User.builder()
                .name("Test Admin")
                .email("testadmin@test.com")
                .password("$2a$12$hash")
                .role(Role.ADMIN)
                .active(true)
                .build());
    }

    // ── Base Tests ──────────────────────────────────────────────

    @Test
    void testBasePersistence() {
        assertThat(base1.getId()).isNotNull();
        assertThat(base1.getName()).isEqualTo("Test Base Alpha");
        assertThat(base1.isActive()).isTrue();
    }

    @Test
    void testFindActiveBasesOnly() {
        base2.setActive(false);
        baseRepository.save(base2);

        var activeBases = baseRepository.findByActiveTrue();
        assertThat(activeBases).hasSize(1);
        assertThat(activeBases.get(0).getName()).isEqualTo("Test Base Alpha");
    }

    @Test
    void testBaseNameUniqueness() {
        assertThat(baseRepository.existsByNameIgnoreCase("Test Base Alpha")).isTrue();
        assertThat(baseRepository.existsByNameIgnoreCase("nonexistent")).isFalse();
    }

    // ── Equipment Type Tests ────────────────────────────────────

    @Test
    void testEquipmentTypePersistence() {
        assertThat(rifle.getId()).isNotNull();
        assertThat(rifle.getName()).isEqualTo("Test Rifle");
        assertThat(rifle.getUnit()).isEqualTo("unit");
    }

    @Test
    void testEquipmentTypeActive() {
        rifle.setActive(false);
        equipmentTypeRepository.save(rifle);

        var activeTypes = equipmentTypeRepository.findByActiveTrue();
        assertThat(activeTypes).isEmpty();
    }

    // ── User Tests ──────────────────────────────────────────────

    @Test
    void testUserPersistence() {
        assertThat(adminUser.getId()).isNotNull();
        assertThat(adminUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void testFindUserByEmail() {
        Optional<User> found = userRepository.findByEmail("testadmin@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Admin");
    }

    @Test
    void testUserEmailExists() {
        assertThat(userRepository.existsByEmail("testadmin@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("nobody@nowhere.com")).isFalse();
    }

    @Test
    void testUserIsUserDetails() {
        assertThat(adminUser.getUsername()).isEqualTo("testadmin@test.com");
        assertThat(adminUser.isEnabled()).isTrue();
        assertThat(adminUser.isAccountNonExpired()).isTrue();
        assertThat(adminUser.getAuthorities()).hasSize(1);
        assertThat(adminUser.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_ADMIN");
    }

    // ── Inventory Tests ─────────────────────────────────────────

    @Test
    void testInventoryCreation() {
        AssetInventory inv = assetInventoryRepository.save(AssetInventory.builder()
                .base(base1)
                .equipmentType(rifle)
                .openingBalance(100)
                .currentQuantity(100)
                .assignedQuantity(0)
                .expendedQuantity(0)
                .build());

        assertThat(inv.getId()).isNotNull();
        assertThat(inv.getCurrentQuantity()).isEqualTo(100);
    }

    @Test
    void testInventoryFindByBaseAndEquipment() {
        assetInventoryRepository.save(AssetInventory.builder()
                .base(base1).equipmentType(rifle)
                .openingBalance(50).currentQuantity(50)
                .assignedQuantity(0).expendedQuantity(0)
                .build());

        Optional<AssetInventory> found = assetInventoryRepository
                .findByBaseIdAndEquipmentTypeId(base1.getId(), rifle.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCurrentQuantity()).isEqualTo(50);
    }

    // ── Purchase Tests ──────────────────────────────────────────

    @Test
    void testPurchaseSumQuery() {
        AssetInventory inv = assetInventoryRepository.save(AssetInventory.builder()
                .base(base1).equipmentType(rifle)
                .openingBalance(0).currentQuantity(0)
                .assignedQuantity(0).expendedQuantity(0)
                .build());

        purchaseRepository.save(Purchase.builder()
                .base(base1).equipmentType(rifle)
                .quantity(50)
                .purchaseDate(LocalDate.now())
                .createdBy(adminUser)
                .build());

        purchaseRepository.save(Purchase.builder()
                .base(base1).equipmentType(rifle)
                .quantity(30)
                .purchaseDate(LocalDate.now())
                .createdBy(adminUser)
                .build());

        int total = purchaseRepository.sumQuantityByBaseId(base1.getId());
        assertThat(total).isEqualTo(80);
    }

    // ── Transfer Tests ──────────────────────────────────────────

    @Test
    void testTransferSumInOut() {
        transferRepository.save(Transfer.builder()
                .sourceBase(base2).destinationBase(base1).equipmentType(rifle)
                .quantity(20).transferDate(LocalDate.now())
                .status(TransferStatus.COMPLETED).createdBy(adminUser)
                .build());

        transferRepository.save(Transfer.builder()
                .sourceBase(base1).destinationBase(base2).equipmentType(rifle)
                .quantity(5).transferDate(LocalDate.now())
                .status(TransferStatus.COMPLETED).createdBy(adminUser)
                .build());

        int transferIn  = transferRepository.sumTransferInByBaseId(base1.getId());
        int transferOut = transferRepository.sumTransferOutByBaseId(base1.getId());

        assertThat(transferIn).isEqualTo(20);
        assertThat(transferOut).isEqualTo(5);
    }

    // ── JWT Tests ───────────────────────────────────────────────

    @Test
    void testJwtGenerationAndValidation() {
        String token = jwtUtils.generateToken(adminUser);
        assertThat(token).isNotBlank();

        String username = jwtUtils.extractUsername(token);
        assertThat(username).isEqualTo("testadmin@test.com");

        assertThat(jwtUtils.isTokenValid(token, adminUser)).isTrue();
    }

    @Test
    void testJwtInvalidForWrongUser() {
        User otherUser = User.builder()
                .name("Other")
                .email("other@test.com")
                .password("pass")
                .role(Role.LOGISTICS_OFFICER)
                .active(true)
                .build();

        String token = jwtUtils.generateToken(adminUser);
        // Token generated for adminUser should not validate for otherUser
        assertThat(jwtUtils.isTokenValid(token, otherUser)).isFalse();
    }
}
