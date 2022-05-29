package com.sdcz.endpass.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sdcz.endpass.R;


public class CustomDialog extends Dialog {

    public CustomDialog(Context context) {
        super(context);
    }

    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;
        private String title;
        private String message;
        private String message2;
        private String hindMessage;
        private String etMessage;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;
        private OnClickListener positiveButtonClickListener;
        private OnClickListener negativeButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setMessage2(String message2) {
            this.message2 = message2;
        }

        public String getEtMessage() {
            return etMessage;
        }

        public void setEtMessage(String hindMessage) {
            this.hindMessage = hindMessage;
        }

//        /**
//         * Set the Dialog message from resource
//         *
//         * @param
//         * @return
//         */
//        public Builder setMessage(int message) {
//            this.message = (String) context.getText(message);
//            return this;
//        }

        /**
         * Set the Dialog title from resource
         *
         * @param title
         * @return
         */
        public void setTitle(int title) {
            this.title = (String) context.getText(title);
        }

        /**
         * Set the Dialog title from String
         *
         * @param title
         * @return
         */

        public void setTitle(String title) {
            this.title = title;
        }
//
//        public Builder setContentView(View v) {
//            this.contentView = v;
//            return this;
//        }

//        /**
//         * Set the positive button resource and it's listener
//         *
//         * @param positiveButtonText
//         * @return
//         */
//        public Builder setPositiveButton(int positiveButtonText,
//                                         OnClickListener listener) {
//            this.positiveButtonText = (String) context
//                    .getText(positiveButtonText);
//            this.positiveButtonClickListener = listener;
//            return this;
//        }

        public void setPositiveButton(String positiveButtonText,
                                         OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
        }
//
//        public Builder setNegativeButton(int negativeButtonText,
//                                         OnClickListener listener) {
//            this.negativeButtonText = (String) context
//                    .getText(negativeButtonText);
//            this.negativeButtonClickListener = listener;
//            return this;
//        }

        public void setNegativeButton(String negativeButtonText,
                                         OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
        }

        public CustomDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final CustomDialog dialog = new CustomDialog(context, R.style.Dialog);
            View layout = inflater.inflate(R.layout.dialog, null);


            dialog.addContentView(layout, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            // set the dialog title
            ((TextView) layout.findViewById(R.id.title)).setText(title);
            // set the confirm button
            if (positiveButtonText != null) {
                ((TextView) layout.findViewById(R.id.positiveTextView))
                        .setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((TextView) layout.findViewById(R.id.positiveTextView))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    etMessage = ((EditText) layout.findViewById(R.id.etMessage)).getText().toString();
                                    positiveButtonClickListener.onClick(dialog,
                                            DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.positiveTextView).setVisibility(
                        View.GONE);
            }
            // set the cancel button
            if (negativeButtonText != null) {
                ((TextView) layout.findViewById(R.id.negativeTextView))
                        .setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((TextView) layout.findViewById(R.id.negativeTextView))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    negativeButtonClickListener.onClick(dialog,
                                            DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.negativeTextView).setVisibility(
                        View.GONE);
            }
            // set the content message
            if (message != null) {
                ((TextView) layout.findViewById(R.id.message)).setText(message);
            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.content))
                        .removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content))
                        .addView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            }

            if (message != null) {
                ((TextView) layout.findViewById(R.id.message)).setText(message);
            } else  {
                // if no message set
                // add the contentView to the dialog body
                ((TextView) layout.findViewById(R.id.message)).setVisibility(View.GONE);
            }


            if (message2 != null) {
                ((TextView) layout.findViewById(R.id.message2)).setText(message2);
            } else  {
                // if no message set
                // add the contentView to the dialog body
                ((TextView) layout.findViewById(R.id.message2)).setVisibility(View.GONE);
            }

            if (hindMessage != null){
                ((EditText) layout.findViewById(R.id.etMessage)).setHint(hindMessage);
            }else {
                /// if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.etMessage).setVisibility(
                        View.GONE);
            }

            dialog.setContentView(layout);
            return dialog;
        }

    }

    @Override
    public void show() {
        super.show();

        Window window = this.getWindow();
        window.setGravity(Gravity.CENTER);
        window.getDecorView().setPadding(40, 40, 40, 40);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }
}
