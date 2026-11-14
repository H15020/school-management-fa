package ir.schoolmanager.fa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ir.schoolmanager.fa.data.local.dao.*
import ir.schoolmanager.fa.data.local.entity.*

@Database(
    entities = [
        SchoolEntity::class,
        AcademicYearEntity::class,
        UserEntity::class,
        StaffEntity::class,
        StudentEntity::class,
        ClassEntity::class,
        SubjectEntity::class,
        EnrollmentEntity::class,
        TeacherAssignmentEntity::class,
        AccessGrantEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SchoolDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao
    abstract fun academicYearDao(): AcademicYearDao
    abstract fun studentDao(): StudentDao
    abstract fun classDao(): ClassDao
    abstract fun userDao(): UserDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun accessGrantDao(): AccessGrantDao

    companion object {
        @Volatile
        private var INSTANCE: SchoolDatabase? = null

        fun getInstance(context: Context): SchoolDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SchoolDatabase::class.java,
                    "school-management.db"
                ).build().also { INSTANCE = it }
            }
    }
}
