package ir.schoolmanager.fa
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import ir.schoolmanager.fa.data.local.SchoolDatabase
import ir.schoolmanager.fa.data.local.entity.ClassEntity
import ir.schoolmanager.fa.data.local.entity.StudentEntity
import kotlinx.coroutines.launch
import java.util.UUID

private const val SCHOOL_ID = "school-zagros"
class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent {
   MaterialTheme(colorScheme = lightColorScheme()) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { SchoolManagerApp() }
   }
  }
 }
}
private enum class AppPage(val title: String, val icon: String) {
 HOME("خانه","⌂"), STUDENTS("دانش‌آموزان","👥"), CLASSES("کلاس‌ها","▦"), ABOUT("درباره برنامه","ⓘ")
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchoolManagerApp() {
 val context = LocalContext.current
 val db = remember { SchoolDatabase.getInstance(context) }
 val scope = rememberCoroutineScope()
 var page by remember { mutableStateOf(AppPage.HOME) }
 var students by remember { mutableStateOf<List<StudentEntity>>(emptyList()) }
 var classes by remember { mutableStateOf<List<ClassEntity>>(emptyList()) }
 var query by remember { mutableStateOf("") }
 var showEditor by remember { mutableStateOf(false) }
 var editingStudent by remember { mutableStateOf<StudentEntity?>(null) }
 var message by remember { mutableStateOf<String?>(null) }
 fun reloadStudents(search: String = query) {
  scope.launch { students = db.studentDao().searchActive(SCHOOL_ID, search) }
 }
 LaunchedEffect(Unit) {
  reloadStudents("")
  val year = db.academicYearDao().getActive(SCHOOL_ID)
  if (year != null) classes = db.classDao().getForYear(year.id)
 }
 LaunchedEffect(query) { reloadStudents(query) }

 Scaffold(
  topBar = {
   TopAppBar(title = {
    Row(verticalAlignment = Alignment.CenterVertically) {
     Image(painterResource(R.drawable.ic_school_management), "نشان برنامه", Modifier.size(36.dp))
     Spacer(Modifier.width(8.dp))
     Text(page.title, fontWeight = FontWeight.Bold)
    }
   })
  },
  bottomBar = {
   Column {
    NavigationBar {
     AppPage.values().forEach { dest ->
      NavigationBarItem(selected = page == dest, onClick = { page = dest },
       icon = { Text(dest.icon, fontSize = 18.sp) }, label = { Text(dest.title, fontSize = 10.sp) })
     }
    }
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
     Text("طراح و توسعه‌دهنده: حسن الله‌مرادی",
      Modifier.fillMaxWidth().padding(vertical = 7.dp, horizontal = 8.dp),
      textAlign = TextAlign.Center, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
   }
  }
 ) { padding ->
  Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp)) {
   when(page) {
    AppPage.HOME -> HomeScreen(students.size, classes.size, { page = AppPage.STUDENTS }, { page = AppPage.CLASSES })
    AppPage.STUDENTS -> StudentsScreen(students, query, { query = it },
     { editingStudent = null; showEditor = true }, { editingStudent = it; showEditor = true },
     { student -> scope.launch {
       db.studentDao().deactivate(student.id, SCHOOL_ID); reloadStudents()
       message = "پرونده دانش‌آموز غیرفعال شد."
     } })
    AppPage.CLASSES -> ClassesScreen(classes)
    AppPage.ABOUT -> AboutScreen(
     { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:allahmorady4@gmail.com"))) },
     { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/H15020"))) })
   }
  }
 }
 if(showEditor) {
  StudentEditorDialog(editingStudent, { showEditor = false }) { code, first, last, father ->
   scope.launch {
    try {
     val old = editingStudent
     if(old == null) {
      db.studentDao().insert(StudentEntity(UUID.randomUUID().toString(), SCHOOL_ID, code.trim(),
       first.trim(), last.trim(), father.trim(), null, true, System.currentTimeMillis()))
      message = "پرونده دانش‌آموز ثبت شد."
     } else {
      db.studentDao().update(old.copy(studentCode=code.trim(), firstName=first.trim(),
       lastName=last.trim(), fatherName=father.trim()))
      message = "اطلاعات دانش‌آموز ویرایش شد."
     }
     showEditor = false
     reloadStudents()
    } catch(e: Exception) {
     message = "ثبت انجام نشد؛ کد دانش‌آموز تکراری است یا خطای پایگاه داده رخ داده است."
    }
   }
  }
 }
 message?.let { msg ->
  AlertDialog(onDismissRequest={message=null}, confirmButton={TextButton(onClick={message=null}){Text("متوجه شدم")}},
   title={Text("پیام برنامه")}, text={Text(msg)})
 }
}

@Composable
private fun HomeScreen(studentCount:Int, classCount:Int, onStudents:()->Unit, onClasses:()->Unit) {
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top=10.dp),
  verticalArrangement=Arrangement.spacedBy(14.dp)) {
  Image(painterResource(R.drawable.ic_school_management), "نشان مدیریت آموزشی",
   Modifier.size(90.dp).align(Alignment.CenterHorizontally))
  Text("سامانه آفلاین مدیریت آموزشی", fontSize=21.sp, fontWeight=FontWeight.Bold)
  Text("سال تحصیلی ۱۴۰۵–۱۴۰۶ | دبیرستان متوسطه اول غیر دولتی زاگرس")
  Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
   SummaryCard("دانش‌آموز فعال", studentCount.toString(), Modifier.weight(1f))
   SummaryCard("کلاس فعال", classCount.toString(), Modifier.weight(1f))
  }
  Button(onClick=onStudents, Modifier.fillMaxWidth()){Text("مدیریت پرونده دانش‌آموزان")}
  OutlinedButton(onClick=onClasses, Modifier.fillMaxWidth()){Text("مشاهده کلاس‌ها")}
  Text("اطلاعات این نسخه روی همین دستگاه ذخیره می‌شود و بدون اینترنت قابل استفاده است. انتقال اطلاعات به دستگاه دیگر پس از افزودن پشتیبان‌گیری ممکن خواهد بود.",
   style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
 }
}
@Composable
private fun SummaryCard(label:String, value:String, modifier:Modifier=Modifier) {
 Card(modifier, elevation=CardDefaults.cardElevation(defaultElevation=1.dp)) {
  Column(Modifier.padding(14.dp), verticalArrangement=Arrangement.spacedBy(5.dp)) {
   Text(value, fontSize=25.sp, fontWeight=FontWeight.Bold); Text(label, fontSize=12.sp)
  }
 }
}
@Composable
private fun StudentsScreen(students:List<StudentEntity>, query:String, onQueryChange:(String)->Unit,
 onAdd:()->Unit, onEdit:(StudentEntity)->Unit, onDeactivate:(StudentEntity)->Unit) {
 Column(Modifier.fillMaxSize().padding(top=8.dp)) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp), verticalAlignment=Alignment.CenterVertically) {
   OutlinedTextField(query,onQueryChange,label={Text("جست‌وجوی نام یا کد")},
    modifier=Modifier.weight(1f),singleLine=true)
   Button(onClick=onAdd){Text("افزودن")}
  }
  Text("تعداد پرونده‌های فعال: " + students.size, Modifier.padding(vertical=10.dp))
  if(students.isEmpty()) {
   Text("هنوز پرونده‌ای ثبت نشده است. برای شروع، دکمه «افزودن» را انتخاب کنید.",
    Modifier.fillMaxWidth().padding(top=32.dp), textAlign=TextAlign.Center,
    color=MaterialTheme.colorScheme.onSurfaceVariant)
  } else {
   LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp), modifier=Modifier.weight(1f)) {
    items(students,key={it.id}) { student ->
     Card(Modifier.fillMaxWidth()) {
      Column(Modifier.padding(12.dp), verticalArrangement=Arrangement.spacedBy(5.dp)) {
       Text(student.firstName + " " + student.lastName,fontWeight=FontWeight.Bold)
       Text("کد: " + student.studentCode + " | نام پدر: " + student.fatherName,fontSize=12.sp)
       Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        TextButton(onClick={onEdit(student)}){Text("ویرایش")}
        TextButton(onClick={onDeactivate(student)}){Text("غیرفعال‌سازی")}
       }
      }
     }
    }
   }
  }
 }
}
@Composable
private fun ClassesScreen(classes:List<ClassEntity>) {
 Column(Modifier.fillMaxSize().padding(top=12.dp)) {
  Text("کلاس‌های سال تحصیلی ۱۴۰۵–۱۴۰۶",fontWeight=FontWeight.Bold,fontSize=18.sp)
  Spacer(Modifier.height(8.dp))
  if(classes.isEmpty()) Text("اطلاعات کلاس‌ها در پایگاه داده موجود نیست.")
  else LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) {
   items(classes,key={it.id}) { item ->
    Card(Modifier.fillMaxWidth()) {
     Row(Modifier.fillMaxWidth().padding(14.dp),horizontalArrangement=Arrangement.SpaceBetween) {
      Text(item.title,fontWeight=FontWeight.Bold); Text("ظرفیت: " + item.capacity)
     }
    }
   }
  }
  Text("در این مرحله فهرست کلاس‌ها فعال است؛ افزودن و ویرایش ساختار کلاس‌ها در مرحله بعد تکمیل می‌شود.",
   Modifier.padding(top=14.dp),style=MaterialTheme.typography.bodySmall)
 }
}
@Composable
private fun AboutScreen(onEmail:()->Unit,onGithub:()->Unit) {
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top=18.dp),
  verticalArrangement=Arrangement.spacedBy(12.dp)) {
  Image(painterResource(R.drawable.ic_school_management),"نشان برنامه",
   Modifier.size(84.dp).align(Alignment.CenterHorizontally))
  Text("درباره برنامه",fontSize=21.sp,fontWeight=FontWeight.Bold)
  Text("مدیریت مدرسه فارسی؛ سامانه آفلاین‌محور برای ثبت و سامان‌دهی امور آموزشی و اجرایی مدارس.")
  Text("طراح و توسعه‌دهنده: حسن الله‌مرادی",fontWeight=FontWeight.Bold)
  Text("پست الکترونیکی: allahmorady4@gmail.com")
  OutlinedButton(onClick=onEmail,Modifier.fillMaxWidth()){Text("ارسال ایمیل به توسعه‌دهنده")}
  Text("شناسه گیت‌هاب: H15020")
  OutlinedButton(onClick=onGithub,Modifier.fillMaxWidth()){Text("مشاهده نمایه گیت‌هاب")}
  Text("نسخه برنامه: ۰٫۳٫۰")
  Text("این برنامه در حال توسعه است. اطلاعات ثبت‌شده فعلاً فقط روی همین دستگاه نگهداری می‌شود.")
 }
}
@Composable
private fun StudentEditorDialog(student:StudentEntity?, onDismiss:()->Unit,
 onSave:(String,String,String,String)->Unit) {
 var code by remember(student?.id){mutableStateOf(student?.studentCode.orEmpty())}
 var first by remember(student?.id){mutableStateOf(student?.firstName.orEmpty())}
 var last by remember(student?.id){mutableStateOf(student?.lastName.orEmpty())}
 var father by remember(student?.id){mutableStateOf(student?.fatherName.orEmpty())}
 val valid=code.isNotBlank() && first.isNotBlank() && last.isNotBlank()
 AlertDialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false),
  modifier=Modifier.fillMaxWidth(0.94f),
  title={Text(if(student==null) "ثبت دانش‌آموز جدید" else "ویرایش پرونده دانش‌آموز")},
  text={
   Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
    OutlinedTextField(code,{code=it},label={Text("کد دانش‌آموزی *")},modifier=Modifier.fillMaxWidth(),singleLine=true)
    OutlinedTextField(first,{first=it},label={Text("نام *")},modifier=Modifier.fillMaxWidth(),singleLine=true)
    OutlinedTextField(last,{last=it},label={Text("نام خانوادگی *")},modifier=Modifier.fillMaxWidth(),singleLine=true)
    OutlinedTextField(father,{father=it},label={Text("نام پدر")},modifier=Modifier.fillMaxWidth(),singleLine=true)
    Text("موارد ستاره‌دار الزامی هستند.",style=MaterialTheme.typography.bodySmall)
   }
  },
  confirmButton={Button(onClick={onSave(code,first,last,father)},enabled=valid){Text("ذخیره")}},
  dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}})
}
