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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import one.mayumi.shruum.model.Wallet;
import one.mayumi.shruum.model.WalletManager;
import timber.log.Timber;

public class VerifySignatureFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify_signature, container, false);
        Timber.d("onCreateView() %s", (String.valueOf(savedInstanceState)));

        view.findViewById(R.id.button_verify_signature).setOnClickListener(view1 -> {
            Wallet wallet = WalletManager.getInstance().getWallet();

            TextInputLayout addressLayout = view.findViewById(R.id.textview_address);
            String address = addressLayout.getEditText().getText().toString();
            TextInputLayout messageLayout = view.findViewById(R.id.textview_message);
            String message = messageLayout.getEditText().getText().toString();
            TextInputLayout signatureLayout = view.findViewById(R.id.textview_signature);
            String signature = signatureLayout.getEditText().getText().toString();

            boolean verified = wallet.verify(message, address, signature);
            Context context = getContext();
            if(context != null) {
                if(verified) {
                    Toast.makeText(context, R.string.valid_signature, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.invalid_signature, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}
