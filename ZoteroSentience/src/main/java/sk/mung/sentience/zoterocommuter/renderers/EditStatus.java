package sk.mung.sentience.zoterocommuter.renderers;


import sk.mung.sentience.zoterocommuter.R;

public enum EditStatus
{
    REMOTE(R.string.attachment_status_on_server),
    SYNCED(R.string.attachment_status_synced),
    LOCAL_UPDATE(R.string.attachment_status_locally_updated),
    SERVER_UPDATE(R.string.attachment_status_server_updated),
    CONFLICT(R.string.attachment_status_conflict),
    EXTRACTING(R.string.attachment_status_extracting);

    private final int resourceId;

    EditStatus(int resourceId)
    {
        this.resourceId = resourceId;
    }

    public int getResourceId()
    {
        return resourceId;
    }
}
