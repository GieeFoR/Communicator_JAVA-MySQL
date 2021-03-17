package Common;

public enum InformationType {
    LOGIN(),
    REGISTER(),
    MESSAGE(),
    FIND_USER_BY_NUMBER(),
    FIND_USER_BY_NICKNAME(),
    NEW_CONVERSATION();

    public boolean checkType(InformationType type) {
        if(this.equals(type)) {
            return true;
        }
        return false;
    }
}
