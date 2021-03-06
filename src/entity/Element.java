package entity;

import com.intellij.psi.xml.XmlTag;
import org.apache.http.util.TextUtils;
import utils.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Element {

    // 判断id正则                                                  android:id="@+id/btn"
    private static final Pattern sIdPattern = Pattern.compile("@\\+?(android:)?id/([^$]+)$", Pattern.CASE_INSENSITIVE);
    // id
    private String id;
    // 名字，如TextView/Button
    private String name;
    // 命名类型 1 aa_bb_cc; 2 aaBbCc; 3 mAaBbCc
    private int fieldNameType = 3;
    private String fieldName;//btn
    private XmlTag xmlTag;
    // 是否生成
    private boolean isCreateFiled = true;
    // 是否Clickable
    private boolean isCreateClickMethod = false;

    /**
     * 构造函数
     *
     * @param name View的名字
     * @param id   android:id属性
     * @throws IllegalArgumentException When the arguments are invalid
     */
    public Element(String name, String id, XmlTag xmlTag) {
        // id
        final Matcher matcher = sIdPattern.matcher(id);
        if (matcher.find() && matcher.groupCount() > 1) {
            this.id = matcher.group(2);
        }

        if (this.id == null) {
            throw new IllegalArgumentException("Invalid format of view id");
        }

        String[] packages = name.split("\\.");
        if (packages.length > 1) {
            // 包名.TextView
            // name="TextView"
            this.name = packages[packages.length - 1];
        } else {
            this.name = name;
        }

        this.xmlTag = xmlTag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFieldNameType() {
        return fieldNameType;
    }

    public void setFieldNameType(int fieldNameType) {
        this.fieldNameType = fieldNameType;
    }

    public XmlTag getXmlTag() {
        return xmlTag;
    }

    public void setXmlTag(XmlTag xmlTag) {
        this.xmlTag = xmlTag;
    }

    // 是否创建Filed属性
    public void setIsCreateFiled(boolean isCreateFiled) {
        this.isCreateFiled = isCreateFiled;
    }

    public boolean isCreateFiled() {
        return isCreateFiled;
    }

    // 是否创建Click方法
    public void setIsCreateClickMethod(boolean isCreateClickMethod) {
        this.isCreateClickMethod = isCreateClickMethod;
    }

    public boolean isCreateClickMethod() {
        return isCreateClickMethod;
    }

    /**
     * 获取id，R.id.xxx
     *
     * @return
     */
    public String getFullID() {
        StringBuilder fullID = new StringBuilder();
        String rPrefix = "R.id.";
        fullID.append(rPrefix);
        fullID.append(id);
        return fullID.toString();
    }

    /**
     * 获取变量名
     *
     * @return
     */
    public String getFieldName() {
        if (TextUtils.isEmpty(this.fieldName)) {
            String fieldName = id;
            String[] names = id.split("_");
            if (fieldNameType == 2) {
                // aaBbCc
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < names.length; i++) {
                    if (i == 0) {
                        sb.append(names[i]);
                    } else {
                        sb.append(Util.firstToUpperCase(names[i]));
                    }
                }
                fieldName = sb.toString();
            } else if (fieldNameType == 3) {
                // mAaBbCc
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < names.length; i++) {
                    if (i == 0) {
                        sb.append("m");
                    }
                    sb.append(Util.firstToUpperCase(names[i]));
                }
                fieldName = sb.toString();
            }
            this.fieldName = fieldName;
        }
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return "Element{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", fieldNameType=" + fieldNameType +
                ", fieldName='" + fieldName + '\'' +
                ", xmlTag=" + xmlTag +
                ", isCreateFiled=" + isCreateFiled +
                ", isCreateClickMethod=" + isCreateClickMethod +
                '}';
    }
}
