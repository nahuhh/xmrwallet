/*
 * Copyright (c) 2017-2019 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// based on https://code.tutsplus.com/tutorials/creating-compound-views-on-android--cms-22889

package one.mayumi.shruum.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.google.android.material.textfield.TextInputLayout;
import one.mayumi.shruum.R;
import one.mayumi.shruum.util.Helper;
import java.util.Locale;

import timber.log.Timber;

public class ExchangeEditText extends LinearLayout {

    private double getEnteredAmount() {
        String enteredAmount = etAmountA.getEditText().getText().toString();
        try {
            return Double.parseDouble(enteredAmount);
        } catch (NumberFormatException ex) {
            Timber.i(ex.getLocalizedMessage());
        }
        return 0;
    }

    public boolean validate(double max, double min) {
        boolean ok = true;
        String nativeAmount = getNativeAmount();
        if (nativeAmount == null) {
            ok = false;
        } else {
            try {
                double amount = Double.parseDouble(nativeAmount);
                if ((amount < min) || (amount > max)) {
                    ok = false;
                }
            } catch (NumberFormatException ex) {
                // this cannot be
                Timber.e(ex.getLocalizedMessage());
                ok = false;
            }
        }
        if (!ok) {
            shakeAmountField();
        }
        return ok;
    }

    void shakeAmountField() {
        etAmountA.startAnimation(Helper.getShakeAnimation(getContext()));
    }

    public void setAmount(String nativeAmount) {
        etAmountA.getEditText().setText(nativeAmount);
    }

    public void setEditable(boolean editable) {
        etAmountA.setEnabled(editable);
    }

    public String getNativeAmount() {
        return getCleanAmountString(etAmountA.getEditText().getText().toString());
    }

    TextInputLayout etAmountA;

    public ExchangeEditText(Context context) {
        super(context);
        initializeViews(context);
    }

    public ExchangeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public ExchangeEditText(Context context,
                            AttributeSet attrs,
                            int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context the current context for the view.
     */
    void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_exchange_edit, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        etAmountA = findViewById(R.id.etAmountA);
    }

    private static final String CLEAN_FORMAT = "%." + Helper.XMR_DECIMALS + "f";

    private String getCleanAmountString(String enteredAmount) {
        try {
            double amount = Double.parseDouble(enteredAmount);
            if (amount >= 0) {
                return String.format(Locale.US, CLEAN_FORMAT, amount);
            } else {
                return null;
            }
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
