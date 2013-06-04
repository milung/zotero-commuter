package sk.mung.sentience.zoterosentience;

import java.util.ArrayList;
import java.util.List;

import android.database.AbstractWindowedCursor;
import android.database.CursorWindow;

public abstract class AbstractWebServiceCursor extends AbstractWindowedCursor
{
    private List<CursorWindow> mWindows = new ArrayList<CursorWindow>();

    protected abstract void loadPageIntoWindow(int pageNumber, int pageSize, CursorWindow window);

    public abstract String[] getColumnNames();

    private int mTotalRows = 0;
    private Integer mTotalRowsLoading = 0;
    private int pageSize = 10;

    public AbstractWebServiceCursor(int pageSize)
    {
        super();
        this.pageSize =pageSize;
        onMove(0,0);
    }

    @Override
    public int getCount()
    {       
        return mTotalRows;
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition)
    {
        if(! isPositionInWindow(newPosition, mWindow))
        {
            mWindow = null;
            for(CursorWindow window : mWindows)
            {
                if(isPositionInWindow( newPosition, window))
                {
                    mWindow = window;
                    break;
                }
            }
        }
        return mWindow != null;
    }

    private boolean isPositionInWindow(int newPosition, CursorWindow window)
    {
        return  window != null && 
                newPosition >= window.getStartPosition() && 
                newPosition < window.getStartPosition() + window.getNumRows();
    }

    /** loads additional data into window 
     * 
     * Loads rows until the cursor amount of rows does not reach the totalRowsReached. 
     * If the total rows count is already reached or being loaded, this method returns immediately;
     * otherwise it fetches data from the server. This method shall be called from the background thread
     * 
     * @param totalRowsReached the row count expected on cursor when loading finishes
     * 
     * 
     **/
    public void loadData(int totalRowsReached)
    {      
        int startPage = 0;  
        int pages = 0;
        
        synchronized (mTotalRowsLoading)
        {
            if(totalRowsReached < mTotalRowsLoading) return;
            else
            {
                int startPosition = mTotalRowsLoading;
                startPage = startPosition/pageSize;
                
                pages 
                    = (totalRowsReached - startPosition)/pageSize 
                    + ((totalRowsReached - startPosition)%pageSize > 0 ? 0 : 1);
                
                mTotalRowsLoading = startPosition + pages * pageSize;               
            }
        }
        // load from service here
        CursorWindow window = new CursorWindow(null);
        window.setNumColumns(getColumnCount());
        window.setStartPosition( startPage * pageSize);
        
        for( int p=0 ; p < pages;  ++p)
        {
            loadPageIntoWindow(startPage, p, window);
        }
        
        addWindow(window);
    }

    private synchronized void addWindow(CursorWindow window)
    {
        mWindows.add(window);
        mTotalRows = Math.max(mTotalRows, window.getStartPosition() + window.getNumRows());
    }

}