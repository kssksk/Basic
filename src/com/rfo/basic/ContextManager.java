/****************************************************************************************************

BASIC! is an implementation of the Basic programming language for
Android devices.

This file is part of BASIC! for Android

Copyright (C) 2015 Paul Laughton

    BASIC! is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BASIC! is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BASIC!.  If not, see <http://www.gnu.org/licenses/>.

    You may contact the author or current maintainers at http://rfobasic.freeforums.org

*************************************************************************************************/

package com.rfo.basic;

import android.content.Context;
import android.util.Log;


/* Keeps track of context for the Interpreter.
 * Knows the current Activity, even if none is awake.
 * Every major Activity must register its state in onPause and onResume.
 */
public class ContextManager {
	public static final String LOGTAG = "ContextManager";

	public static final int ACTIVITY_NONE	= 0;
	public static final int ACTIVITY_APP	= 1; // for setting mAppContext
	public static final int ACTIVITY_RUN	= 2; // Console
	public static final int ACTIVITY_GR		= 3; // Graphics
	public static final int ACTIVITY_WEB	= 4; // HTML
	public static final int ACTIVITY_OTHER	= 5;

	private int mCurrentActivity = ACTIVITY_NONE;

	// Record the Context of each major Activity (Run, GR, Web).
	// These MUST be set to null on exit!
	private Context[] mContext = { null,  null,  null,  null,  null,  null };
	// For each major Activity, set paused state true in onPause, false in onResume.
	private boolean[] mPaused  = { false, false, false, false, false, false };

	public ContextManager(Context appContext) {
		registerContext(ACTIVITY_APP, appContext);
	}

	private int normalize(int activity) {
		return ((activity >= 0) && (activity < ACTIVITY_OTHER)) ? activity : ACTIVITY_OTHER;
	}

	public Context getContext() {
		Context context = getContext(mCurrentActivity);
		if (context == null) {
			// Assume GR or WEB is shutting down, so return the RUN context.
			// TODO: Don't assume. Add a backstack?
			context = getContext(ACTIVITY_RUN);
			if (context == null) { throw new IllegalStateException("No context"); }
		}
		return context;
	}

	public synchronized Context getContext(int activity) {
		return mContext[activity];
	}

	public synchronized void registerContext(int activity, Context context) {
		activity = normalize(activity);
		mContext[activity] = context;
		Log.d(LOGTAG, "setContext of " + activity + " to " + context);
	}

	// Unregister Context of an Activity, if that Context is still valid.
	public synchronized void unregisterContext(int activity, Context context) {
		activity = normalize(activity);
		if (mContext[activity] == context) {
			mContext[activity] = null;
			if (activity == mCurrentActivity) {	// if it is the current Activity
				setCurrent(ACTIVITY_NONE);		// clear the current Activity setting
				Log.d(LOGTAG, "Current activity (" + activity + ") cleared");
			}
			Log.d(LOGTAG, "Context of " + activity + " cleared");
		}
	}

	public synchronized void setCurrent(int activity) {
		activity = normalize(activity);
		mCurrentActivity = activity;
		Log.d(LOGTAG, "New current activity " + activity);
	}

	// Every major Activity updates its state here in onPause.
	// At present this information is not used.
	public synchronized void onPause(int activity) {
		activity = normalize(activity);
		mPaused[activity] = true;
		Log.d(LOGTAG, "Pause activity " + activity);
	}

	// Every major Activity updates its state here in onResume.
	// At present the Paused information is not used.
	public synchronized void onResume(int activity) {
		activity = normalize(activity);
		mPaused[activity] = false;
		Log.d(LOGTAG, "Resume activity " + activity + ", current was " + mCurrentActivity);
		if (activity != mCurrentActivity) { setCurrent(activity); }
	}

	public void clear() {						// null all the Context references
		int n = mContext.length;
		for (int i = 0; i < n; ++i) { mContext[i] = null; }
		mCurrentActivity = ACTIVITY_NONE;
	}
}
