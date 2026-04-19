package fast.campus.netplix.exception;

public class UserException extends NetplixException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static class UserAlreadyExistException extends UserException {
        public UserAlreadyExistException() {
            super(ErrorCode.USER_ALREADY_EXIST);
        }
    }

    public static class UserDoesNotExistException extends UserException {
        public UserDoesNotExistException() {
            super(ErrorCode.USER_DOES_NOT_EXIST);
        }
    }

    public static class InvalidUsernameException extends UserException {
        public InvalidUsernameException() {
            super(ErrorCode.INVALID_USERNAME);
        }
    }

    public static class InvalidEmailFormatException extends UserException {
        public InvalidEmailFormatException() {
            super(ErrorCode.INVALID_EMAIL_FORMAT);
        }
    }

    public static class InvalidPhoneFormatException extends UserException {
        public InvalidPhoneFormatException() {
            super(ErrorCode.INVALID_PHONE_FORMAT);
        }
    }
}
