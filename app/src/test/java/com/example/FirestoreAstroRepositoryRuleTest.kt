package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.FirestoreAstroRepository
import com.example.data.KundliBirthDetailsRepository
import com.example.model.BirthDetails
import com.example.model.KundliProfileFirestore
import com.example.model.UserProfileFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

class FirestoreAstroRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private val aliceEmail = "alice_test@example.com"
    private val bobEmail = "bob_test@example.com"

    @org.junit.Before
    override fun setUpFirebase() {
        super.setUpFirebase()
        org.junit.Assume.assumeTrue(
            "Firestore and Auth emulators must be running for live network tests",
            isPortOpen(FIRESTORE_PORT) && isPortOpen(AUTH_PORT)
        )
    }

    @Test
    fun testUserProfileSaveAndRetrieve() = runBlocking {
        val aliceUid = signInTestUser(aliceEmail)
        val repo = FirestoreAstroRepository(firestore)

        val profile = UserProfileFirestore(
            id = aliceUid,
            email = aliceEmail,
            displayName = "Alice Astrologer",
            preferredLanguage = "en",
            aiMemoryEnabled = true
        )

        val saveResult = repo.saveUserProfile(profile)
        assertTrue("Saving valid user profile should succeed", saveResult.isSuccess)

        val retrieved = withTimeout(5000L) {
            repo.observeUserProfile(aliceUid).first { it != null }
        }
        assertNotNull(retrieved)
        assertEquals("Alice Astrologer", retrieved?.displayName)
        assertEquals(aliceEmail, retrieved?.email)
    }

    @Test
    fun testKundliProfileSaveAndList() = runBlocking {
        val aliceUid = signInTestUser(aliceEmail)
        val repo = FirestoreAstroRepository(firestore)

        val kundli = KundliProfileFirestore(
            id = "kundli_alice_1",
            userId = aliceUid,
            name = "Alice Kundli Chart",
            birthDate = "1992-04-14",
            birthTime = "10:30",
            birthPlace = "Varanasi, India",
            gender = "female"
        )

        val saveResult = repo.saveKundliProfile(kundli)
        assertTrue("Saving valid Kundli profile should succeed", saveResult.isSuccess)

        val list = withTimeout(5000L) {
            repo.observeKundliProfiles(aliceUid).first { it.isNotEmpty() }
        }
        assertTrue("List must contain saved profile", list.any { it.id == "kundli_alice_1" })
        val savedItem = list.first { it.id == "kundli_alice_1" }
        assertEquals("Alice Kundli Chart", savedItem.name)
        assertEquals("Varanasi, India", savedItem.birthPlace)
    }

    @Test
    fun testBirthDetailsStorageAndKundliCalculation() = runBlocking {
        val aliceUid = signInTestUser(aliceEmail)
        val birthRepo = KundliBirthDetailsRepository(firestore)

        val profileWithBirth = UserProfileFirestore(
            id = aliceUid,
            email = aliceEmail,
            displayName = "Alice Seeker",
            preferredLanguage = "en",
            aiMemoryEnabled = true,
            birthDate = "1995-10-24",
            birthTime = "06:45",
            birthPlace = "New Delhi, India",
            latitude = 28.6139,
            longitude = 77.2090,
            timezoneOffset = 5.5,
            gender = "female",
            isTimeApproximate = false
        )

        val saveResult = birthRepo.saveUserProfile(profileWithBirth)
        assertTrue("Saving profile with birth details must succeed", saveResult.isSuccess)

        val fetchedProfile = withTimeout(5000L) {
            birthRepo.observeUserProfile(aliceUid).first { it?.hasValidBirthDetails() == true }
        }
        assertNotNull(fetchedProfile)
        assertEquals("1995-10-24", fetchedProfile?.birthDate)
        assertEquals("06:45", fetchedProfile?.birthTime)
        assertEquals("New Delhi, India", fetchedProfile?.birthPlace)

        // Compute Kundli using repository
        val kundliChart = birthRepo.calculateKundliForUserProfile(fetchedProfile!!)
        assertNotNull("Kundli calculation from stored profile must not be null", kundliChart)
        assertTrue(kundliChart!!.lagnaSignName.isNotBlank())
        assertTrue(kundliChart.planets.isNotEmpty())
        assertEquals(12, kundliChart.houses.size)

        // Update birth details directly via repository
        val newBirthDetails = BirthDetails(
            name = "Alice Seeker",
            birthDate = "1996-01-15",
            birthTime = "14:20",
            birthPlace = "Mumbai, India",
            latitude = 19.0760,
            longitude = 72.8777
        )
        val updateResult = birthRepo.updateUserBirthDetails(aliceUid, newBirthDetails)
        assertTrue(updateResult.isSuccess)

        val updatedProfile = birthRepo.getUserProfile(aliceUid).getOrNull()
        assertEquals("1996-01-15", updatedProfile?.birthDate)
        assertEquals("14:20", updatedProfile?.birthTime)
        assertEquals("Mumbai, India", updatedProfile?.birthPlace)
    }

    @Test
    fun testCrossUserIsolation() = runBlocking {
        val aliceUid = signInTestUser(aliceEmail)
        val repoAlice = FirestoreAstroRepository(firestore)

        val kundli = KundliProfileFirestore(
            id = "kundli_alice_secret",
            userId = aliceUid,
            name = "Alice Secret Chart",
            birthDate = "1990-01-01",
            birthTime = "12:00",
            birthPlace = "Haridwar, India"
        )
        val saveResult = repoAlice.saveKundliProfile(kundli)
        assertTrue(saveResult.isSuccess)

        // Sign in as Bob
        signInTestUser(bobEmail)
        val repoBob = FirestoreAstroRepository(firestore)

        // Bob tries to read Alice's Kundli profile document directly via Firestore
        val bobAttempt = repoBob.observeKundliProfiles(aliceUid).first()
        // Firestore security rules block this, resulting in an error caught by repo and returning emptyList()
        assertTrue("Bob must not be able to observe Alice's Kundli profiles", bobAttempt.isEmpty())
    }
}
