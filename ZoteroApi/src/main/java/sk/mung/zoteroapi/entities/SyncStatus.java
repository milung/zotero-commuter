package sk.mung.zoteroapi.entities;


public enum SyncStatus
{
    SYNC_UNKNOWN(0),
    SYNC_OK(1),
    SYNC_LOCALLY_UPDATED(2),
    SYNC_CONFLICT(3),
    SYNC_DELETED(4),
    SYNC_DELETED_CONFLICT(5),
    SYNC_REMOTE_VERSION(6),
    SYNC_ATTACHMENT_CONFLICT(7),
    SYNC_ATTACHMENT_TOO_BIG(8),
    SYNC_ATTACHMENT_NOT_AUTHORIZED(9);

    private final int statusCode;


    private SyncStatus(int statusCode)
    {
        this.statusCode = statusCode;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public static SyncStatus fromStatusCode(int statusCode)
    {
        for(SyncStatus status : values())
        {
            if(status.getStatusCode() == statusCode)
            {
                return status;
            }
        }
        return SYNC_UNKNOWN;
    }
}
