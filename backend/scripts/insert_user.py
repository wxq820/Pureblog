import bcrypt
import pymysql

pw = '123456'
new_hash = bcrypt.hashpw(pw.encode('utf-8'), bcrypt.gensalt(rounds=10)).decode()
stored = '{bcrypt}' + new_hash
print(f'Hash length: {len(stored)}')

conn = pymysql.connect(host='127.0.0.1', port=13306, user='root', password='123456', database='pureblog')
cur = conn.cursor()
cur.execute('DELETE FROM pb_user WHERE username=%s', ('wxq820',))
cur.execute(
    'INSERT INTO pb_user (username, email, password_hash, nickname, role, status, created_at, updated_at) '
    'VALUES (%s, %s, %s, %s, %s, %s, NOW(), NOW())',
    ('wxq820', 'wxq820@pureblog.local', stored, 'WXQ', 3, 1)
)
conn.commit()
cur.execute('SELECT id, username, nickname, role, LENGTH(password_hash) FROM pb_user WHERE username=%s', ('wxq820',))
print('Inserted:', cur.fetchone())
cur.close()
conn.close()
