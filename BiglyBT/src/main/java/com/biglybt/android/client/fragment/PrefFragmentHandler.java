/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.biglybt.android.client.fragment;

import com.biglybt.android.client.*;
import com.biglybt.android.client.activity.SessionActivity;
import com.biglybt.android.client.dialog.DialogFragmentNumberPicker;
import com.biglybt.android.client.session.RemoteProfile;
import com.biglybt.android.client.session.Session;
import com.biglybt.android.client.session.SessionSettings;
import com.biglybt.util.DisplayFormatters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

/**
 * Created by TuxPaper on 10/22/17.
 */

public class PrefFragmentHandler
{

	static final String KEY_SESSION_DOWNLOAD = "session_download";

	static final String KEY_SESSION_UPLOAD = "session_upload";

	static final String KEY_SESSION_DOWNLOAD_MANUAL = "session_download_maual";

	static final String KEY_SESSION_UPLOAD_MANUAL = "session_upload_manual";

	static final String KEY_SESSION_DOWNLOAD_LIMIT = "session_download_limit";

	static final String KEY_SESSION_UPLOAD_LIMIT = "session_upload_limit";

	static final String KEY_PROFILE_NICKNAME = "nickname";

	static final String KEY_SHOW_OPEN_OPTIONS = "show_open_options";

	static final String KEY_SMALL_LIST = "small_list";

	private static final String TAG = "PrefFragmentHandler";

	protected final SessionActivity activity;

	PreferenceDataStoreMap dataStore;

	private PreferenceManager preferenceManager;

	public PrefFragmentHandler(SessionActivity activity) {
		this.activity = activity;
	}

	public void onCreate(Bundle savedInstanceState,
			PreferenceManager preferenceManager) {
		this.preferenceManager = preferenceManager;
		dataStore = new PreferenceDataStoreMap();
		fillDataStore();

		preferenceManager.setPreferenceDataStore(dataStore);
	}

	public boolean onPreferenceTreeClick(Preference preference) {
		switch (preference.getKey()) {
			case "remote_connection": {
				Session session = activity.getSession();
				if (session != null) {
					RemoteUtils.editProfile(session.getRemoteProfile(),
							activity.getSupportFragmentManager());
				}

//					// TODO: Update nick if user changes it
//					// Really TODO: Don't use edit profile dialog
				return true;
			}

			case KEY_SESSION_DOWNLOAD: {
				DialogFragmentNumberPicker.NumberPickerBuilder builder = new DialogFragmentNumberPicker.NumberPickerBuilder(
						activity.getSupportFragmentManager(), KEY_SESSION_DOWNLOAD,
						dataStore.getInt(KEY_SESSION_DOWNLOAD_LIMIT, 0)).setTitleId(
								R.string.rp_download_speed).setMin(0).setMax(99999).setSuffix(
										R.string.kbps).setClearButtonText(
												R.string.unlimited).set3rdButtonText(
														R.string.button_autospeed);
				DialogFragmentNumberPicker.openDialog(builder);
				return true;
			}

			case KEY_SESSION_UPLOAD: {
				DialogFragmentNumberPicker.NumberPickerBuilder builder = new DialogFragmentNumberPicker.NumberPickerBuilder(
						activity.getSupportFragmentManager(), KEY_SESSION_UPLOAD,
						dataStore.getInt(KEY_SESSION_UPLOAD_LIMIT, 0)).setTitleId(
								R.string.rp_upload_speed).setMin(0).setMax(99999).setSuffix(
										R.string.kbps).setClearButtonText(
												R.string.unlimited).set3rdButtonText(
														R.string.button_autospeed);
				DialogFragmentNumberPicker.openDialog(builder);

				return true;
			}

			case KEY_PROFILE_NICKNAME: {
				final Session session = activity.getSession();
				if (session != null) {
					AlertDialog dialog = AndroidUtilsUI.createTextBoxDialog(activity,
							R.string.profile_nickname,
							(session.getRemoteProfile().getRemoteType() == RemoteProfile.TYPE_CORE)
									? R.string.profile_nick_explain
									: R.string.profile_localnick_explain,
							session.getRemoteProfile().getNick(), EditorInfo.IME_ACTION_DONE,
							new AndroidUtilsUI.OnTextBoxDialogClick() {

								@Override
								public void onClick(DialogInterface dialog, int which,
										EditText editText) {
									final String newName = editText.getText().toString();

									session.getRemoteProfile().setNick(newName);
									session.triggerSessionSettingsChanged();
									fillDataStore();
								}
							});
					dialog.show();
				}
				return true;
			}

			case KEY_SMALL_LIST: {
				final Session session = activity.getSession();
				if (session != null) {
					session.getRemoteProfile().setUseSmallLists(
							((SwitchPreference) preference).isChecked());
					session.triggerSessionSettingsChanged();
				}
				return true;
			}
		}
		return false;
	}

	public void fillDataStore() {
		Session session = activity.getSession();
		if (session == null) {
			return;
		}
		SessionSettings sessionSettings = session.getSessionSettingsClone();
		if (sessionSettings == null) {
			return;
		}

		String s;
		Resources resources = activity.getResources();
		RemoteProfile profile = session.getRemoteProfile();

		boolean dlManual = sessionSettings.isDlManual();
		long dlSpeedK = sessionSettings.getManualDlSpeed();
		dataStore.putBoolean(PrefFragmentHandler.KEY_SESSION_DOWNLOAD_MANUAL, dlManual);
		dataStore.putLong(PrefFragmentHandler.KEY_SESSION_DOWNLOAD_LIMIT, dlSpeedK);
		if (dlManual) {
			s = resources.getString(R.string.setting_speed_on_summary,
					DisplayFormatters.formatByteCountToKiBEtcPerSec(dlSpeedK * 1024));
		} else {
			s = resources.getString(R.string.unlimited);
		}
		findPreference(PrefFragmentHandler.KEY_SESSION_DOWNLOAD).setSummary(s);

		boolean ulManual = sessionSettings.isUlManual();
		dataStore.putBoolean(PrefFragmentHandler.KEY_SESSION_UPLOAD_MANUAL, ulManual);
		dataStore.putBoolean(PrefFragmentHandler.KEY_SESSION_UPLOAD_LIMIT, ulManual);
		long ulSpeedK = sessionSettings.getManualUlSpeed();
		if (ulManual) {
			s = resources.getString(R.string.setting_speed_on_summary,
					DisplayFormatters.formatByteCountToKiBEtcPerSec(ulSpeedK * 1024));
		} else {
			s = resources.getString(R.string.unlimited);
		}
		findPreference(PrefFragmentHandler.KEY_SESSION_UPLOAD).setSummary(s);

		String nick = profile.getNick();
		dataStore.putString(PrefFragmentHandler.KEY_PROFILE_NICKNAME, nick);
		findPreference(PrefFragmentHandler.KEY_PROFILE_NICKNAME).setSummary(nick);

		// Refresh Interval... TODO

		boolean useSmallLists = profile.useSmallLists();
		dataStore.putBoolean(PrefFragmentHandler.KEY_SMALL_LIST, useSmallLists);
		((SwitchPreference) findPreference(PrefFragmentHandler.KEY_SMALL_LIST)).setChecked(
				useSmallLists);

		boolean addTorrentSilently = profile.isAddTorrentSilently();
		dataStore.putBoolean(PrefFragmentHandler.KEY_SHOW_OPEN_OPTIONS,
				!addTorrentSilently);
		((SwitchPreference) findPreference(
				PrefFragmentHandler.KEY_SHOW_OPEN_OPTIONS)).setChecked(!addTorrentSilently);

		findPreference("ps_main").setTitle("Settings for " + nick);
	}

	public void onNumberPickerChange(@Nullable String callbackID, int val) {
		Log.d(TAG, "onNumberPickerChange() called with: callbackID = [" + callbackID
				+ "], val = [" + val + "]");

		Session session = activity.getSession();
		if (session == null) {
			return;
		}
		SessionSettings sessionSettings = session.getSessionSettingsClone();
		if (sessionSettings == null) {
			return;
		}

		if (PrefFragmentHandler.KEY_SESSION_DOWNLOAD.equals(callbackID)) {
			sessionSettings.setDLIsManual(val > 0);
			if (val > 0) {
				sessionSettings.setManualDlSpeed(val);
			}
			session.updateSessionSettings(sessionSettings);
		}
		if (PrefFragmentHandler.KEY_SESSION_UPLOAD.equals(callbackID)) {
			sessionSettings.setULIsManual(val > 0);
			if (val > 0) {
				sessionSettings.setManualUlSpeed(val);
			}
			session.updateSessionSettings(sessionSettings);
		}

		fillDataStore();
	}

	public Preference findPreference(String key) {
		return preferenceManager.findPreference(key);
	}
}
