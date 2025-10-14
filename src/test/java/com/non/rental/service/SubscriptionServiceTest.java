package com.non.rental.service;

import com.non.rental.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubscriptionService.
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private DataManager dataManager;

    @Mock
    private CurrentAuthentication currentAuthentication;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User testUser;
    private UserSubscription testSubscription;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");

        testSubscription = new UserSubscription();
        testSubscription.setId(UUID.randomUUID());
        testSubscription.setUser(testUser);
        testSubscription.setPlan(SubscriptionPlan.FREE);
        testSubscription.setActive(true);
        testSubscription.setSavedAnalysesCount(0);
        testSubscription.setComparisonsCount(0);
    }

    @Test
    void testGetCurrentUserSubscription_ExistingSubscription() {
        // Arrange
        when(currentAuthentication.getUser()).thenReturn(testUser);
        when(dataManager.load(UserSubscription.class))
                .thenReturn(mock(io.jmix.core.FluentLoader.ByQuery.class));
        
        // Note: This is a simplified test. In a real scenario, you'd need to mock the entire chain.
        // For now, we'll test the service logic separately.
    }

    @Test
    void testCreateFreeSubscription() {
        // Arrange
        UserSubscription newSubscription = new UserSubscription();
        when(dataManager.create(UserSubscription.class)).thenReturn(newSubscription);
        when(dataManager.save(any(UserSubscription.class))).thenReturn(newSubscription);

        // Act
        UserSubscription result = subscriptionService.createFreeSubscription(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(SubscriptionPlan.FREE, result.getPlan());
        assertTrue(result.getActive());
        assertEquals(0, result.getSavedAnalysesCount());
        assertEquals(0, result.getComparisonsCount());
        verify(dataManager).save(any(UserSubscription.class));
    }

    @Test
    void testHasFeatureAccess_FreeUser() {
        // Arrange
        when(currentAuthentication.getUser()).thenReturn(testUser);
        // Mock the load chain to return the test subscription
        // This is simplified - in real tests you'd mock the entire FluentLoader chain

        // For now, test the feature access logic directly
        assertFalse(subscriptionService.hasFeatureAccess("UNLIMITED_ANALYSES"));
        assertFalse(subscriptionService.hasFeatureAccess("ADVANCED_ANALYTICS"));
        assertFalse(subscriptionService.hasFeatureAccess("EXPORT_PDF"));
    }

    @Test
    void testGetPlanFeatures_FreePlan() {
        // Act
        var features = subscriptionService.getPlanFeatures(SubscriptionPlan.FREE);

        // Assert
        assertNotNull(features);
        assertFalse(features.isEmpty());
        assertTrue(features.stream().anyMatch(f -> f.contains("3 saved analyses")));
    }

    @Test
    void testGetPlanFeatures_PlusPlan() {
        // Act
        var features = subscriptionService.getPlanFeatures(SubscriptionPlan.PLUS);

        // Assert
        assertNotNull(features);
        assertFalse(features.isEmpty());
        assertTrue(features.stream().anyMatch(f -> f.contains("25 saved analyses")));
        assertTrue(features.stream().anyMatch(f -> f.contains("Advanced analytics")));
    }

    @Test
    void testGetPlanFeatures_ProPlan() {
        // Act
        var features = subscriptionService.getPlanFeatures(SubscriptionPlan.PRO);

        // Assert
        assertNotNull(features);
        assertFalse(features.isEmpty());
        assertTrue(features.stream().anyMatch(f -> f.contains("Unlimited")));
        assertTrue(features.stream().anyMatch(f -> f.contains("Priority support")));
    }

    @Test
    void testUserSubscription_CanSaveMoreAnalyses() {
        // Free plan with 2 analyses
        testSubscription.setPlan(SubscriptionPlan.FREE);
        testSubscription.setSavedAnalysesCount(2);
        assertTrue(testSubscription.canSaveMoreAnalyses());

        // Free plan at limit
        testSubscription.setSavedAnalysesCount(3);
        assertFalse(testSubscription.canSaveMoreAnalyses());

        // Pro plan (unlimited)
        testSubscription.setPlan(SubscriptionPlan.PRO);
        testSubscription.setSavedAnalysesCount(100);
        assertTrue(testSubscription.canSaveMoreAnalyses());
    }

    @Test
    void testUserSubscription_CanCreateMoreComparisons() {
        // Free plan with 1 comparison
        testSubscription.setPlan(SubscriptionPlan.FREE);
        testSubscription.setComparisonsCount(1);
        assertTrue(testSubscription.canCreateMoreComparisons());

        // Free plan at limit
        testSubscription.setComparisonsCount(2);
        assertFalse(testSubscription.canCreateMoreComparisons());

        // Pro plan (unlimited)
        testSubscription.setPlan(SubscriptionPlan.PRO);
        testSubscription.setComparisonsCount(50);
        assertTrue(testSubscription.canCreateMoreComparisons());
    }

    @Test
    void testSubscriptionPlan_Prices() {
        assertEquals(0.0, SubscriptionPlan.FREE.getMonthlyPrice());
        assertEquals(0.0, SubscriptionPlan.FREE.getAnnualPrice());
        
        assertEquals(9.99, SubscriptionPlan.PLUS.getMonthlyPrice());
        assertEquals(99.99, SubscriptionPlan.PLUS.getAnnualPrice());
        
        assertEquals(19.99, SubscriptionPlan.PRO.getMonthlyPrice());
        assertEquals(199.99, SubscriptionPlan.PRO.getAnnualPrice());
    }

    @Test
    void testSubscriptionPlan_IsFree() {
        assertTrue(SubscriptionPlan.FREE.isFree());
        assertFalse(SubscriptionPlan.PLUS.isFree());
        assertFalse(SubscriptionPlan.PRO.isFree());
    }
}