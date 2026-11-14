package ir.schoolmanager.fa.data.local.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ir.schoolmanager.fa.data.local.entity.*

@Dao
interface SchoolDao {
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(school: SchoolEntity)
 @Query("SELECT * FROM schools WHERE isActive = 1 ORDER BY name") suspend fun getActiveSchools(): List<SchoolEntity>
 @Query("SELECT * FROM schools WHERE id = :id LIMIT 1") suspend fun getById(id: String): SchoolEntity?
}
@Dao
interface AcademicYearDao {
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(year: AcademicYearEntity)
 @Query("SELECT * FROM academic_years WHERE schoolId = :schoolId ORDER BY startDateEpochDay DESC") suspend fun getForSchool(schoolId: String): List<AcademicYearEntity>
 @Query("SELECT * FROM academic_years WHERE schoolId = :schoolId AND isActive = 1 LIMIT 1") suspend fun getActive(schoolId: String): AcademicYearEntity?
 @Query("UPDATE academic_years SET isActive = 0 WHERE schoolId = :schoolId") suspend fun deactivateAll(schoolId: String)
 @Query("UPDATE academic_years SET isActive = 1 WHERE id = :yearId AND schoolId = :schoolId AND isArchived = 0") suspend fun activate(schoolId: String, yearId: String): Int
}
@Dao
interface StudentDao {
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(student: StudentEntity)
 @Update suspend fun update(student: StudentEntity)
 @Query("""SELECT * FROM students WHERE schoolId = :schoolId AND isActive = 1
 AND (:query = '' OR firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' OR studentCode LIKE '%' || :query || '%')
 ORDER BY lastName, firstName""")
 suspend fun searchActive(schoolId: String, query: String): List<StudentEntity>
 @Query("UPDATE students SET isActive = 0 WHERE id = :studentId AND schoolId = :schoolId") suspend fun deactivate(studentId: String, schoolId: String): Int
 @Query("SELECT * FROM students WHERE id = :studentId LIMIT 1") suspend fun getById(studentId: String): StudentEntity?
}
@Dao
interface ClassDao {
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(classEntity: ClassEntity)
 @Query("SELECT * FROM school_classes WHERE academicYearId = :yearId AND isActive = 1 ORDER BY gradeLevel, section") suspend fun getForYear(yearId: String): List<ClassEntity>
}
@Dao
interface UserDao {
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(user: UserEntity)
 @Query("SELECT * FROM users WHERE schoolId = :schoolId AND username = :username AND isActive = 1 LIMIT 1") suspend fun findActiveByUsername(schoolId: String, username: String): UserEntity?
}
@Dao
interface AuditLogDao {
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(log: AuditLogEntity)
 @Query("SELECT * FROM audit_logs WHERE schoolId = :schoolId ORDER BY createdAt DESC LIMIT :limit") suspend fun latest(schoolId: String, limit: Int): List<AuditLogEntity>
}
@Dao
interface AccessGrantDao {
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(grant: AccessGrantEntity)
 @Query("""SELECT * FROM access_grants WHERE userId = :userId AND academicYearId = :yearId
 AND startsAt <= :now AND endsAt >= :now AND revokedAt IS NULL ORDER BY startsAt DESC""")
 suspend fun getCurrentGrants(userId: String, yearId: String, now: Long): List<AccessGrantEntity>
}
