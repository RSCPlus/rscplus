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
		if(rscplus.getInstance().isApplet())
			return rscplus.getInstance().isActive();
		else
			return true;
	}
}
