package kr.petworld.petworld

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kr.petworld.petworld.databinding.ActivityEditInformationBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.view.MenuItem




@SuppressLint("Registered")
class EditInfomationActivity : AppCompatActivity() {


    var mainImagePath: String? = null
    private var mYear: Int = 0
    var mMonth: Int = 0
    var mDay: Int = 0
    var mHour: Int = 0
    var mMinute: Int = 0
    lateinit var activityEditInformationBinding: ActivityEditInformationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityEditInformationBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_edit_information)
        activityEditInformationBinding!!.executePendingBindings()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)


        // edit=false-> 등록하기, edit->true 수정하기
        var edit = intent.getBooleanExtra("edit", false)
        if (edit) {
            activityEditInformationBinding.createAndSave.text = "수정하기"
        }

        //show pet name
        activityEditInformationBinding.dogName.setText(
            PreferencesManager.getInstance(this).getValue(
                String::class.java,
                PreferencesManager.Key.dogName,
                ""
            ) as String?
        )
        //반려견 견종
        activityEditInformationBinding.dogDesc.setText(
            PreferencesManager.getInstance(this).getValue(
                String::class.java,
                PreferencesManager.Key.dogDescription,
                ""
            ) as String?
        )

        //반려견 이미지 경로 보여주기
        var path = PreferencesManager.getInstance(this).getValue(
            String::class.java,
            PreferencesManager.Key.mainImagePath,
            ""
        ) as String?
        if (path != null) {
            Picasso.get().load(File(path)).into(activityEditInformationBinding.mainImage)
        }
        //갤러리에서 이미지 선택
        activityEditInformationBinding.mainImage.setOnClickListener {
            //runtime permission 체크
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, EditInfomationActivity.PERMISSION_CODE);
                } else {
                    //permission already granted
                    pickImageFromGallery();
                }
            } else {

                pickImageFromGallery()
            }
        }

        val c = Calendar.getInstance()


        // 생년월일 선택하기
        activityEditInformationBinding.selectBirthDay.setOnClickListener {
            // Get Current Date

            mYear = c.get(Calendar.YEAR)
            mMonth = c.get(Calendar.MONTH)
            mDay = c.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    activityEditInformationBinding.selectBirthDay.text =
                        dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                        //year + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString()

                }, mYear, mMonth, mDay
            )
            datePickerDialog.show()
        }

        //성별 보여주기
        activityEditInformationBinding.male.isSelected = (
                PreferencesManager.getInstance(this).getValue(
                    String::class.java,
                    PreferencesManager.Key.gender,
                    "male"
                ) as String) == "male"

        activityEditInformationBinding.female.isSelected = (
                PreferencesManager.getInstance(this).getValue(
                    String::class.java,
                    PreferencesManager.Key.gender,
                    "male"
                ) as String) == "female"
        //성별 선택
        activityEditInformationBinding.male.setOnClickListener {
            activityEditInformationBinding.male.isSelected = true
            activityEditInformationBinding.female.isSelected = false
        }
        activityEditInformationBinding.female.setOnClickListener {
            activityEditInformationBinding.male.isSelected = false
            activityEditInformationBinding.female.isSelected = true
        }
        //생일 보여주기
        var birthday = PreferencesManager.getInstance(this).getValue<String>(
            String::class.java,
            PreferencesManager.Key.birthday,
            ""
        ) as String?

        if (birthday != null && birthday.length > 0) {
            var simpleFormatter = SimpleDateFormat("dd-MM-yyyy")
            var birthDate = simpleFormatter.parse(birthday)
            c.timeInMillis = birthDate.time
            activityEditInformationBinding.selectBirthDay.text = birthday
        }

        //반려견 정보 생성 or 수정
        activityEditInformationBinding.createAndSave.setOnClickListener {


            var dgname = activityEditInformationBinding.dogName.text.toString()
            var dgDesc = activityEditInformationBinding.dogDesc.text.toString()
            var birthday = activityEditInformationBinding.selectBirthDay.text.toString()
            //이름
            PreferencesManager.getInstance(this).setValue(PreferencesManager.Key.dogName, dgname)
            //견종
            PreferencesManager.getInstance(this)
                .setValue(PreferencesManager.Key.dogDescription, dgDesc)
            //성별
            PreferencesManager.getInstance(this)
                .setValue(
                    PreferencesManager.Key.gender,
                    if (activityEditInformationBinding.male.isSelected) "male" else "female"
                )
            //생일
            PreferencesManager.getInstance(this)
                .setValue(PreferencesManager.Key.birthday, birthday)
            if (mainImagePath != null) {
                PreferencesManager.getInstance(this)
                    .setValue(PreferencesManager.Key.mainImagePath, mainImagePath!!)
            }
            //set registred true
            PreferencesManager.getInstance(this)
                .setValue(PreferencesManager.Key.registered, "yeap")
            finish()


        }


    }

    //되돌아가기
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }


    //갤러리에서 이미지 선택
    private fun pickImageFromGallery() {
        //선택한 이미지 넘겨주기
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, EditInfomationActivity.IMAGE_PICK_CODE)
    }


    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup granted
                    pickImageFromGallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000 && resultCode == RESULT_OK && null != data) {
            //activityContentBinding.mainImage.setImageURI(data?.data)
            // 이미지 잘라내기
            CropImage.activity(data?.data)
                .start(this)

        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            var result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                var resultUri = result.getUri()
                activityEditInformationBinding.mainImage.setImageURI(resultUri)
                mainImagePath = FileUtils.getFile(this@EditInfomationActivity, resultUri)!!.path

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                var error = result.error;
            }
        }
    }
}