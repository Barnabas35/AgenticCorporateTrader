
def new_session_token():
    from random import choice
    from string import ascii_letters, digits

    return "".join(choice(ascii_letters + digits) for _ in range(64))
