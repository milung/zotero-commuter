package sk.mung.sanity;


public class SanityCheck
{
    static public void checkArgumentNotNull(Object argument, String name)
    {
        if( argument == null)
        {
            throw new IllegalArgumentException("Argument is null: " +  name);
        }
    }
}
