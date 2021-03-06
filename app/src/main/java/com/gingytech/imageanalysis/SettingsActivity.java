package com.gingytech.imageanalysis;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the
 * <a href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the device
	 * configuration dictates that a simplified, single-pane UI should be shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		addPreferencesFromResource(R.xml.pref_image);
		bindPreferenceSummaryToValue(findPreference("setting_image_width"));
		bindPreferenceSummaryToValue(findPreference("setting_image_height"));
//		bindPreferenceSummaryToValue(findPreference("setting_image_bitcount"));
//		bindPreferenceSummaryToValue(findPreference("setting_image_isptype"));
		bindPreferenceSummaryToValue(findPreference("setting_image_drawline_width"));
		bindPreferenceSummaryToValue(findPreference("setting_image_sensorposition_left"));
		bindPreferenceSummaryToValue(findPreference("setting_image_sensorposition_top"));
		bindPreferenceSummaryToValue(findPreference("setting_image_sensorposition_rotate"));
		bindPreferenceSummaryToValue(findPreference("setting_image_sensingarea_color_r"));
		bindPreferenceSummaryToValue(findPreference("setting_image_sensingarea_color_g"));
		bindPreferenceSummaryToValue(findPreference("setting_image_sensingarea_color_b"));
		bindPreferenceSummaryToValue(findPreference("setting_image_sensingarea_width"));
		bindPreferenceSummaryToValue(findPreference("setting_image_sensingarea_height"));
		bindPreferenceSummaryToValue(findPreference("setting_image_sensingarea_size"));
		bindPreferenceSummaryToValue(findPreference("setting_image_enrollnum"));
		bindPreferenceSummaryToValue(findPreference("setting_image_fingerqualityscore"));
		bindPreferenceSummaryToValue(findPreference("setting_enroll_overlapscore_all"));
		bindPreferenceSummaryToValue(findPreference("setting_enroll_overlapscore_last"));
	}

	/**
	 * A preference value change listener that updates the preference's summary to
	 * reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
			}else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the preference
	 * title) is updated to reflect the value. The summary is also immediately
	 * updated upon calling this method. The exact display format is dependent on
	 * the type of preference.
	 *
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
				.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
	}


}
