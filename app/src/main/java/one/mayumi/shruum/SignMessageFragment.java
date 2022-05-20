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

package one.mayumi.shruum;

import android.content.Context;
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
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import one.mayumi.shruum.data.BarcodeData;
import one.mayumi.shruum.data.Crypto;
import one.mayumi.shruum.data.TxData;
import one.mayumi.shruum.data.UserNotes;
import one.mayumi.shruum.model.PendingTransaction;
import one.mayumi.shruum.model.Wallet;
import one.mayumi.shruum.model.WalletManager;
import one.mayumi.shruum.util.Helper;
import one.mayumi.shruum.util.OpenAliasHelper;
import one.mayumi.shruum.util.ServiceHelper;

import java.util.Map;

import timber.log.Timber;

public class SignMessageFragment extends Fragment {

    private String signature;
    private String address;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_message, container, false);
        Timber.d("onCreateView() %s", (String.valueOf(savedInstanceState)));
        Wallet wallet = WalletManager.getInstance().getWallet();
        this.address = wallet.getAddress();
        TextView tvAddress = view.findViewById(R.id.textview_address);
        tvAddress.setText(wallet.getAddress());

        view.findViewById(R.id.button_sign_message).setOnClickListener(view1 -> {
            TextInputLayout messageLayout = view.findViewById(R.id.textview_message_to_sign);
            String message = messageLayout.getEditText().getText().toString();
            String signature = wallet.sign(message);
            TextView tvSignature = view.findViewById(R.id.textview_signature);
            tvSignature.setText(signature);
            view.findViewById(R.id.button_copy_signature).setVisibility(View.VISIBLE);
            this.signature = signature;
        });

        view.findViewById(R.id.button_copy_address).setOnClickListener(view1 -> copyAddress());
        view.findViewById(R.id.button_copy_signature).setOnClickListener(view1 -> copySignature());
        return view;
    }

    void copySignature() {
        Helper.clipBoardCopy(requireActivity(), getString(R.string.label_copy_signature), signature);
        Toast.makeText(getActivity(), getString(R.string.message_copy_signature), Toast.LENGTH_SHORT).show();
    }

    void copyAddress() {
        Helper.clipBoardCopy(requireActivity(), getString(R.string.label_copy_address), address);
        Toast.makeText(getActivity(), getString(R.string.message_copy_address), Toast.LENGTH_SHORT).show();
    }
}
