package sk.mung.sentience.zoterosentience.renderers;


import sk.mung.sentience.zoterosentience.R;

public enum EditStatus
{
    REMOTE(R.string.attachment_status_on_server),
    SYNCED(R.string.attachment_status_synced),
    LOCAL_UPDATE(R.string.attachment_status_locally_updated),
    SERVER_UPDATE(R.string.attachment_status_server_updated),
    CONFLICT(R.string.attachment_status_conflict);

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
