
def firestore_safe(user_string):

    # Remove leading and trailing whitespace
    user_string.strip()

    # Replace all instances of "." with "[dot]"
    user_string = user_string.replace(".", "[dot]")

    # Replace all instances of "/" with "[slash]"
    user_string = user_string.replace("/", "[slash]")

    # Replace all instances of "\" with "[backslash]"
    user_string = user_string.replace("\\", "[backslash]")

    return user_string


def firestore_unsafe(user_string):

    # Replace all instances of "[dot]" with "."
    user_string = user_string.replace("[dot]", ".")

    # Replace all instances of "[slash]" with "/"
    user_string = user_string.replace("[slash]", "/")

    # Replace all instances of "[backslash]" with "\"
    user_string = user_string.replace("[backslash]", "\\")

    return user_string
