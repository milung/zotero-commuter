package sk.mung.sentience.zoterocommuter.storage;

import sk.mung.zoteroapi.entities.Field;

/**
 * Created by sk1u00e5 on 1.7.2013.
 */
class FieldLazyProxy extends Field
{
    private final FieldsDao fieldsDao;

    public FieldLazyProxy(FieldsDao fieldsDao)
    {
        this.fieldsDao = fieldsDao;
    }

    @Override
    public void setValue(String value)
    {
        if(!value.equals(getValue()))
        {
            super.setValue(value);
            if(this.getId() != 0)
            {
                fieldsDao.update(this);
            }
        }
    }
}
