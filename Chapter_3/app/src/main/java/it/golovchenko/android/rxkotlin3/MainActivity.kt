package it.golovchenko.android.rxkotlin3

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.combineLatest
import it.golovchenko.android.rxkotlin3.utils.CardType
import it.golovchenko.android.rxkotlin3.utils.checkCardChecksum
import it.golovchenko.android.rxkotlin3.utils.isValidCvc
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private val mCreditCardType by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.credit_card_type) }
    private val mCreditCardNumber by lazy(LazyThreadSafetyMode.NONE) { findViewById<EditText>(R.id.credit_card_number) }
    private val mExpirationDate by lazy(LazyThreadSafetyMode.NONE) { findViewById<EditText>(R.id.expiration_date) }
    private val mCreditCardCvc by lazy(LazyThreadSafetyMode.NONE) { findViewById<EditText>(R.id.credit_card_cvc) }
    private val mSubmitButton by lazy(LazyThreadSafetyMode.NONE) { findViewById<Button>(R.id.submit_button) }
    private val mErrorText by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.error_text) }
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val cardNumber = mCreditCardNumber.textChanges().map(CharSequence::toString)

        val cvc = mCreditCardCvc.textChanges().map(CharSequence::toString)
        val expirationDate = mExpirationDate.textChanges().map(CharSequence::toString)

        val cardType = cardNumber.map { CardType.fromNumber(it) }
        val isCardTypeKnow = cardType.map { it != CardType.UNKNOWN }
        val isCheckSumValid = cardNumber.map(::checkCardChecksum)

        val isCardNumberValid = Observables.combineLatest(isCardTypeKnow, isCheckSumValid)
        { isExistingType, isCorrectChecksum -> isExistingType && isCorrectChecksum }

        val isCvcValid = Observables.combineLatest(cardType, cvc, ::isValidCvc)

        val expirationDatePattern = Pattern.compile("^\\d\\d/\\d\\d$")
        val isExpirationDateValid = expirationDate.map { expirationDatePattern.matcher(it).matches() }
        compositeDisposable.addAll(
            Observables.combineLatest(isCardNumberValid, isCvcValid, isExpirationDateValid)
            { isValidCardNumber, isValidCvc, isValidExpirationData ->
                isValidCardNumber && isValidCvc && isValidExpirationData
            }.observeOn(AndroidSchedulers.mainThread()).subscribe(mSubmitButton::setEnabled)
            ,
            cardType.map { it.name.toUpperCase() }.observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCreditCardType::setText)
            ,
            listOf(
                isCardTypeKnow.map { it.toError("Unknown card type") },
                isCheckSumValid.map { it.toError("Wrong CheckSum") },
                isCvcValid.map { it.toError("Invalid CVC") },
                isExpirationDateValid.map { it.toError("Wrong Date") }
            ).combineLatest { errors -> errors.filter { it.isNotBlank() }.joinToString("\n") }
                .subscribe(mErrorText::setText)
        )
        listOf(
            (mCreditCardNumber to isCardNumberValid),
            (mCreditCardCvc to isCvcValid),
            (mExpirationDate to isExpirationDateValid)
        ).forEach { it.showErrorForText() }
    }

    private fun Pair<EditText, Observable<Boolean>>.showErrorForText() =
        Observables.combineLatest(this.first.focusChanges(), this.second)
        { hasFocusValue, isValidValue -> (!hasFocusValue && !isValidValue) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.first.setTextColor(if (it) Color.RED else Color.BLACK)
            }


    private fun Boolean.toError(e: String) = if (this) "" else e

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

}

