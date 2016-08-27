/**
 *	rscplus
 *
 *	This file is part of rscplus.
 *
 *	rscplus is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	rscplus is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with rscplus.  If not, see <http://www.gnu.org/licenses/>.
 *
 *	Authors: see <https://github.com/OrN/rscplus>
 */

package rscplus;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.URL;

public class RSC_AppletStub implements AppletStub
{
	@Override
	public final void appletResize(int width, int height)
	{
	}

	@Override
	public final AppletContext getAppletContext()
	{
		return null;
	}

	@Override
	public final String getParameter(String key)
	{
		return rscplus.getInstance().getJConfig().parameters.get(key);
	}

	@Override
	public final URL getCodeBase()
	{
		return rscplus.getInstance().getJConfig().getURL("codebase");
	}

	@Override
	public final URL getDocumentBase()
	{
		return getCodeBase();
	}

	@Override
	public boolean isActive()
	{
		return true;
	}
}
