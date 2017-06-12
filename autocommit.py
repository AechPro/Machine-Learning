from subprocess import call
call('git add *.py',shell=True)
call('git commit -a -m "Automated Commit..."',shell=True)
call('git push -u origin master',shell=True)
