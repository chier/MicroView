import math
import json
import sys

def func_simple(argStr,exp):
    result = ''
    arg = json.loads(argStr)
    try:
        result = eval(exp)
    except TypeError,e:
        result = '--'
    return result

def func_segmented(argStr,expStr):
    result = ''
    exp = ''
    arg = json.loads(argStr)
    exp_conditions = json.loads(expStr)
    try:
        for ec in exp_conditions:
            if(eval(ec["condition"])):
                exp = ec["exp"]
                break
        if(exp == ''):
            result = '--'
        else:
            result = eval(exp)
    except TypeError,e:
        result = '--'
    return result
      
if __name__ == '__main__':
    r = func_segmented(sys.argv[1],sys.argv[2])
    print r
