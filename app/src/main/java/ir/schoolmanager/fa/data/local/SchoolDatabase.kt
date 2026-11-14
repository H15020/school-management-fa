package ir.schoolmanager.fa.data.local
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.schoolmanager.fa.data.local.dao.*
import ir.schoolmanager.fa.data.local.entity.*
import java.time.LocalDate

@Database(entities = [SchoolEntity::class, AcademicYearEntity::class, UserEntity::class, StaffEntity::class,
 StudentEntity::class, ClassEntity::class, SubjectEntity::class, EnrollmentEntity::class,
 TeacherAssignmentEntity::class, AccessGrantEntity::class, AuditLogEntity::class],
 version = 1, exportSchema = false)
abstract class SchoolDatabase : RoomDatabase() {
 abstract fun schoolDao(): SchoolDao
 abstract fun academicYearDao(): AcademicYearDao
 abstract fun studentDao(): StudentDao
 abstract fun classDao(): ClassDao
 abstract fun userDao(): UserDao
 abstract fun auditLogDao(): AuditLogDao
 abstract fun accessGrantDao(): AccessGrantDao

 companion object {
  private const val SCHOOL_ID = "school-zagros"
  private const val YEAR_ID = "year-1405-1406"
  @Volatile private var INSTANCE: SchoolDatabase? = null

  private val seedCallback = object : Callback() {
   override fun onCreate(db: SupportSQLiteDatabase) {
    super.onCreate(db)
    db.execSQL("INSERT INTO schools (id,name,province,city,address,phone,createdAt,isActive) VALUES (?,?,?,?,?,?,?,1)",
     arrayOf(SCHOOL_ID, "دبیرستان متوسطه اول غیر دولتی زاگرس", "کردستان", "سنندج",
      "شهرک بهاران، بلوار رازی، جنب پارک علم و فناوری", "۰۸۷۳۳۷۷۲۶۰۰", System.currentTimeMillis()))
    db.execSQL("INSERT INTO academic_years (id,schoolId,title,startDateEpochDay,endDateEpochDay,isActive,isArchived) VALUES (?,?,?,?,?,1,0)",
     arrayOf(YEAR_ID, SCHOOL_ID, "۱۴۰۵–۱۴۰۶", LocalDate.of(2026,9,23).toEpochDay(), LocalDate.of(2027,6,21).toEpochDay()))
    val classes = listOf(
     arrayOf<Any>("class-7-1",7,"۱","هفتم ۱",25),
     arrayOf<Any>("class-7-2",7,"۲","هفتم ۲",25),
     arrayOf<Any>("class-8-1",8,"۱","هشتم",30),
     arrayOf<Any>("class-9-1",9,"۱","نهم",30))
    classes.forEach { item ->
     db.execSQL("INSERT INTO school_classes (id,schoolId,academicYearId,gradeLevel,section,title,capacity,isActive) VALUES (?,?,?,?,?,?,?,1)",
      arrayOf(item[0], SCHOOL_ID, YEAR_ID, item[1], item[2], item[3], item[4]))
    }
   }
  }
  fun getInstance(context: Context): SchoolDatabase = INSTANCE ?: synchronized(this) {
   INSTANCE ?: Room.databaseBuilder(context.applicationContext, SchoolDatabase::class.java, "school-management.db")
    .addCallback(seedCallback).build().also { INSTANCE = it }
  }
 }
}
