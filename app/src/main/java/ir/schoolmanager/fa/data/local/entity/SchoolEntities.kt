package ir.schoolmanager.fa.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "schools")
data class SchoolEntity(
    @PrimaryKey val id: String,
    val name: String,
    val province: String,
    val city: String,
    val address: String,
    val phone: String,
    val createdAt: Long,
    val isActive: Boolean = true
)

@Entity(
    tableName = "academic_years",
    foreignKeys = [
        ForeignKey(
            entity = SchoolEntity::class,
            parentColumns = ["id"],
            childColumns = ["schoolId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("schoolId"), Index(value = ["schoolId", "title"], unique = true)]
)
data class AcademicYearEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val title: String,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long,
    val isActive: Boolean = false,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = SchoolEntity::class,
            parentColumns = ["id"],
            childColumns = ["schoolId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("schoolId"), Index(value = ["schoolId", "username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val fullName: String,
    val username: String,
    val passwordHash: String,
    val passwordSalt: String,
    val role: String,
    val isActive: Boolean = true,
    val createdAt: Long
)

@Entity(
    tableName = "staff",
    foreignKeys = [
        ForeignKey(
            entity = SchoolEntity::class,
            parentColumns = ["id"],
            childColumns = ["schoolId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("schoolId"), Index("userId")]
)
data class StaffEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val userId: String?,
    val fullName: String,
    val position: String,
    val phone: String,
    val isActive: Boolean = true
)

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = SchoolEntity::class,
            parentColumns = ["id"],
            childColumns = ["schoolId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("schoolId"), Index(value = ["schoolId", "studentCode"], unique = true)]
)
data class StudentEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val studentCode: String,
    val firstName: String,
    val lastName: String,
    val fatherName: String,
    val birthDateEpochDay: Long?,
    val isActive: Boolean = true,
    val createdAt: Long
)

@Entity(
    tableName = "school_classes",
    foreignKeys = [
        ForeignKey(entity = SchoolEntity::class, parentColumns = ["id"], childColumns = ["schoolId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = AcademicYearEntity::class, parentColumns = ["id"], childColumns = ["academicYearId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("schoolId"), Index("academicYearId"), Index(value = ["academicYearId", "gradeLevel", "section"], unique = true)]
)
data class ClassEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val academicYearId: String,
    val gradeLevel: Int,
    val section: String,
    val title: String,
    val capacity: Int,
    val isActive: Boolean = true
)

@Entity(
    tableName = "subjects",
    indices = [Index(value = ["schoolId", "title"], unique = true)]
)
data class SubjectEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val title: String,
    val isActive: Boolean = true
)

@Entity(
    tableName = "enrollments",
    foreignKeys = [
        ForeignKey(entity = StudentEntity::class, parentColumns = ["id"], childColumns = ["studentId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = ClassEntity::class, parentColumns = ["id"], childColumns = ["classId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = AcademicYearEntity::class, parentColumns = ["id"], childColumns = ["academicYearId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("studentId"), Index("classId"), Index("academicYearId"), Index(value = ["studentId", "academicYearId"], unique = true)]
)
data class EnrollmentEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val classId: String,
    val academicYearId: String,
    val enrollmentDateEpochDay: Long,
    val status: String = "ACTIVE"
)

@Entity(
    tableName = "teacher_assignments",
    foreignKeys = [
        ForeignKey(entity = StaffEntity::class, parentColumns = ["id"], childColumns = ["staffId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = ClassEntity::class, parentColumns = ["id"], childColumns = ["classId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = SubjectEntity::class, parentColumns = ["id"], childColumns = ["subjectId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = AcademicYearEntity::class, parentColumns = ["id"], childColumns = ["academicYearId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("staffId"), Index("classId"), Index("subjectId"), Index("academicYearId")]
)
data class TeacherAssignmentEntity(
    @PrimaryKey val id: String,
    val staffId: String,
    val classId: String,
    val subjectId: String,
    val academicYearId: String
)

@Entity(
    tableName = "access_grants",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["grantedByUserId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = AcademicYearEntity::class, parentColumns = ["id"], childColumns = ["academicYearId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId"), Index("academicYearId"), Index("grantedByUserId")]
)
data class AccessGrantEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val academicYearId: String,
    val classId: String?,
    val subjectId: String?,
    val permissionMode: String,
    val startsAt: Long,
    val endsAt: Long,
    val grantedByUserId: String,
    val revokedAt: Long? = null
)

@Entity(
    tableName = "audit_logs",
    indices = [Index("schoolId"), Index("actorUserId"), Index("createdAt")]
)
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val actorUserId: String?,
    val action: String,
    val targetType: String,
    val targetId: String?,
    val details: String,
    val createdAt: Long
)
