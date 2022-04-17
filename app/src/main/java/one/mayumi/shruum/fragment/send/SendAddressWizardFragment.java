/*
 * Copyright (c) 2017 m2049r
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

package one.mayumi.shruum.fragment.send;

import android.content.Context;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;
import one.mayumi.shruum.R;
import one.mayumi.shruum.data.BarcodeData;
import one.mayumi.shruum.data.Crypto;
import one.mayumi.shruum.data.TxData;
import one.mayumi.shruum.data.UserNotes;
import one.mayumi.shruum.model.PendingTransaction;
import one.mayumi.shruum.model.Wallet;
import one.mayumi.shruum.util.Helper;
import one.mayumi.shruum.util.OpenAliasHelper;
import one.mayumi.shruum.util.ServiceHelper;

import java.util.Map;

import timber.log.Timber;

public class SendAddressWizardFragment extends SendWizardFragment {

    static final int INTEGRATED_ADDRESS_LENGTH = 106;

    public static SendAddressWizardFragment newInstance(Listener listener) {
        SendAddressWizardFragment instance = new SendAddressWizardFragment();
        instance.setSendListener(listener);
        return instance;
    }

    Listener sendListener;

    public void setSendListener(Listener listener) {
        this.sendListener = listener;
    }

    public interface Listener {
        void setBarcodeData(BarcodeData data);

        BarcodeData getBarcodeData();

        BarcodeData popBarcodeData();

        void setMode(SendFragment.Mode mode);

        TxData getTxData();
    }

    private EditText etDummy;
    private TextInputLayout etAddress;
    private TextInputLayout etNotes;
    private Crypto selectedCrypto = null;

    private boolean resolvingOA = false;

    OnScanListener onScanListener;

    public interface OnScanListener {
        void onScan();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Timber.d("onCreateView() %s", (String.valueOf(savedInstanceState)));

        View view = inflater.inflate(R.layout.fragment_send_address, container, false);

        etAddress = view.findViewById(R.id.etAddress);
        etAddress.getEditText().setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        etAddress.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // ignore ENTER
                return ((event != null) && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER));
            }
        });
        etAddress.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String enteredAddress = etAddress.getEditText().getText().toString().trim();
                String dnsOA = dnsFromOpenAlias(enteredAddress);
                Timber.d("OpenAlias is %s", dnsOA);
                if (dnsOA != null) {
                    processOpenAlias(dnsOA);
                }
            }
        });
        etAddress.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                Timber.d("AFTER: %s", editable.toString());
                etAddress.setError(null);
                selectedCrypto = null;
                final String address = etAddress.getEditText().getText().toString();
                if (isIntegratedAddress(address)) {
                    Timber.d("isIntegratedAddress");
                    selectedCrypto = Crypto.XMR;
                    etAddress.setError(getString(R.string.info_paymentid_integrated));
                    sendListener.setMode(SendFragment.Mode.XMR);
                } else if (isStandardAddress(address)) {
                    Timber.d("isStandardAddress");
                    selectedCrypto = Crypto.XMR;
                    sendListener.setMode(SendFragment.Mode.XMR);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        final ImageButton bPasteAddress = view.findViewById(R.id.bPasteAddress);
        bPasteAddress.setOnClickListener(v -> {
            final String clip = Helper.getClipBoardText(getActivity());
            if (clip == null) return;
            // clean it up
            final String address = clip.replaceAll("( +)|(\\r?\\n?)", "");
            BarcodeData bc = BarcodeData.fromString(address);
            if (bc != null) {
                processScannedData(bc);
                final EditText et = etAddress.getEditText();
                et.setSelection(et.getText().length());
                etAddress.requestFocus();
            } else {
                Toast.makeText(getActivity(), getString(R.string.send_address_invalid), Toast.LENGTH_SHORT).show();
            }
        });

        etNotes = view.findViewById(R.id.etNotes);
        etNotes.getEditText().setRawInputType(InputType.TYPE_CLASS_TEXT);
        etNotes.getEditText().

                setOnEditorActionListener((v, actionId, event) -> {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))
                            || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        etDummy.requestFocus();
                        return true;
                    }
                    return false;
                });

        final View cvScan = view.findViewById(R.id.bScan);
        cvScan.setOnClickListener(v -> onScanListener.onScan());

        etDummy = view.findViewById(R.id.etDummy);
        etDummy.setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        etDummy.requestFocus();

        View tvNfc = view.findViewById(R.id.tvNfc);
        NfcManager manager = (NfcManager) getContext().getSystemService(Context.NFC_SERVICE);
        if ((manager != null) && (manager.getDefaultAdapter() != null))
            tvNfc.setVisibility(View.VISIBLE);

        return view;
    }

    private void processOpenAlias(String dnsOA) {
        if (resolvingOA) return; // already resolving - just wait
        sendListener.popBarcodeData();
        if (dnsOA != null) {
            resolvingOA = true;
            etAddress.setError(getString(R.string.send_address_resolve_openalias));
            OpenAliasHelper.resolve(dnsOA, new OpenAliasHelper.OnResolvedListener() {
                @Override
                public void onResolved(Map<Crypto, BarcodeData> dataMap) {
                    resolvingOA = false;
                    BarcodeData barcodeData = dataMap.get(Crypto.XMR);
                    if (barcodeData != null) {
                        Timber.d("Security=%s, %s", barcodeData.security.toString(), barcodeData.address);
                        processScannedData(barcodeData);
                    } else {
                        etAddress.setError(getString(R.string.send_address_not_openalias));
                        Timber.d("NO XMR OPENALIAS TXT FOUND");
                    }
                }

                @Override
                public void onFailure() {
                    resolvingOA = false;
                    etAddress.setError(getString(R.string.send_address_not_openalias));
                    Timber.e("OA FAILED");
                }
            });
        } // else ignore
    }

    private boolean checkAddressNoError() {
        return selectedCrypto != null;
    }

    private boolean checkAddress() {
        boolean ok = checkAddressNoError();
        if (!ok) {
            etAddress.setError(getString(R.string.send_address_invalid));
        } else {
            etAddress.setError(null);
        }
        return ok;
    }

    private boolean isStandardAddress(String address) {
        return Wallet.isAddressValid(address);
    }

    private boolean isIntegratedAddress(String address) {
        return (address.length() == INTEGRATED_ADDRESS_LENGTH)
                && Wallet.isAddressValid(address);
    }

    private void shakeAddress() {
        etAddress.startAnimation(Helper.getShakeAnimation(getContext()));
    }

    @Override
    public boolean onValidateFields() {
        if (!checkAddressNoError()) {
            shakeAddress();
            String enteredAddress = etAddress.getEditText().getText().toString().trim();
            String dnsOA = dnsFromOpenAlias(enteredAddress);
            Timber.d("OpenAlias is %s", dnsOA);
            if (dnsOA != null) {
                processOpenAlias(dnsOA);
            }
            return false;
        }

        if (sendListener != null) {
            TxData txData = sendListener.getTxData();
            txData.setDestinationAddress(etAddress.getEditText().getText().toString());
            ServiceHelper.ASSET = null;
            txData.setUserNotes(new UserNotes(etNotes.getEditText().getText().toString()));
            txData.setPriority(PendingTransaction.Priority.Priority_Default);
            txData.setMixin(SendFragment.MIXIN);
        }
        return true;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnScanListener) {
            onScanListener = (OnScanListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement ScanListener");
        }
    }

    // QR Scan Stuff

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume");
        processScannedData();
    }

    public void processScannedData(BarcodeData barcodeData) {
        sendListener.setBarcodeData(barcodeData);
        if (isResumed())
            processScannedData();
    }

    public void processScannedData() {
        BarcodeData barcodeData = sendListener.getBarcodeData();
        if (barcodeData != null) {
            Timber.d("GOT DATA");
            if (!Helper.ALLOW_SHIFT && (barcodeData.asset != Crypto.XMR)) {
                Timber.d("BUT ONLY XMR SUPPORTED");
                barcodeData = null;
                sendListener.setBarcodeData(barcodeData);
                return;
            }
            if (barcodeData.address != null) {
                etAddress.getEditText().setText(barcodeData.address);
                selectedCrypto = null;
                if (barcodeData.isAmbiguous()) {
                } else {
                    selectedCrypto = barcodeData.asset;
                }
                if (checkAddress()) {
                    if (barcodeData.security == BarcodeData.Security.OA_NO_DNSSEC)
                        etAddress.setError(getString(R.string.send_address_no_dnssec));
                    else if (barcodeData.security == BarcodeData.Security.OA_DNSSEC)
                        etAddress.setError(getString(R.string.send_address_openalias));
                }
            } else {
                etAddress.getEditText().getText().clear();
                etAddress.setError(null);
            }

            String scannedNotes = barcodeData.addressName;
            if (scannedNotes == null) {
                scannedNotes = barcodeData.description;
            } else if (barcodeData.description != null) {
                scannedNotes = scannedNotes + ": " + barcodeData.description;
            }
            if (scannedNotes != null) {
                etNotes.getEditText().setText(scannedNotes);
            } else {
                etNotes.getEditText().getText().clear();
                etNotes.setError(null);
            }
        } else
            Timber.d("barcodeData=null");
    }

    @Override
    public void onResumeFragment() {
        super.onResumeFragment();
        Timber.d("onResumeFragment()");
        etDummy.requestFocus();
    }

    String dnsFromOpenAlias(String openalias) {
        Timber.d("checking openalias candidate %s", openalias);
        if (Patterns.DOMAIN_NAME.matcher(openalias).matches()) return openalias;
        if (Patterns.EMAIL_ADDRESS.matcher(openalias).matches()) {
            openalias = openalias.replaceFirst("@", ".");
            if (Patterns.DOMAIN_NAME.matcher(openalias).matches()) return openalias;
        }
        return null; // not an openalias
    }
}
